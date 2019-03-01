#include "mainwindow.h"
#include <QMessageBox>
#include <QWebEngineProfile>
#include "Coordinates.h"
#include "Geohash.h"
#include "SensorController.h"
#include <QFile>

static const QString g_mapDiv = "megamap";
static const QString g_baseHtml = "<!DOCTYPE html>\n"
                                  "<html>\n"
                                  "<head>\n"
                                  "  <style>\n"
                                  "     #%1 {\n"
                                  "      height: 650px;\n"
                                  "      width: 100%;\n"
                                  "     }\n"
                                  "  </style>\n"
                                  "</head>\n"
                                  "<body>\n"
                                  "  <h3>My Google Maps Demo</h3>\n"
                                  "  <div id=\"%1\"></div>\n"
                                  "  <script>\n"
                                  "    function initMap() {\n"
                                  "      var bishkek = {lat: %3, lng: %4};\n"
                                  "      var map = new google.maps.Map(document.getElementById('%1'), {\n"
                                  "        zoom: 14,\n"
                                  "        center: bishkek\n"
                                  "      });\n"
                                  "       %2\n"
                                  "    }\n"                                  "  </script>\n"
                                  "  <script async defer\n"
                                  "  src=\"https://maps.googleapis.com/maps/api/js?callback=initMap\">\n"
                                  "  </script>\n"
                                  "</body>\n"
                                  "</html>\n";

MainWindow::MainWindow(const QString &coordsFilePath,
                       const QString &filteredFilePath,
                       const QString &filteredFilePath2,
                       QWidget *parent)
  : QMainWindow(parent),
    m_view(new QWebEngineView(this)),
    m_coordsFilePath(coordsFilePath),
    m_filteredFilePath(filteredFilePath),
    m_filteredFilePath2(filteredFilePath2)
{
  this->setGeometry(0, 0, 800, 750);
  setCentralWidget(m_view);  
  QWebEnginePage *page = m_view->page();  

  connect(page, &QWebEnginePage::featurePermissionRequested,
          [this, page](const QUrl &securityOrigin, QWebEnginePage::Feature feature) {
    if (feature != QWebEnginePage::Geolocation)
      return;

    QMessageBox msgBox(this);
    msgBox.setText(tr("%1 wants to know your location").arg(securityOrigin.host()));
    msgBox.setInformativeText(tr("Do you want to send your current location to this website?"));
    msgBox.setStandardButtons(QMessageBox::Yes | QMessageBox::No);
    msgBox.setDefaultButton(QMessageBox::Yes);

    if (msgBox.exec() == QMessageBox::Yes) {
      page->setFeaturePermission(
            securityOrigin, feature, QWebEnginePage::PermissionGrantedByUser);
    } else {
      page->setFeaturePermission(
            securityOrigin, feature, QWebEnginePage::PermissionDeniedByUser);
    }
  });

  initMap(page, m_coordsFilePath, m_filteredFilePath, m_filteredFilePath2);
}
//////////////////////////////////////////////////////////////////////////

static QString jsCoordsString(const std::vector<geopoint_t>& lst,
                              const QString& prefix,
                              const QString& color) {
  QString coordsStr = QString("var lst%1 = {\n").arg(prefix);
  for (size_t i = 0; i < lst.size(); ++i) {
    coordsStr += QString("%1coord%2: {\n"
                         "  center: {lat: %3, lng: %4}\n"
                         "},\n ").arg(prefix).arg(i).arg(lst[i].Latitude).arg(lst[i].Longitude);
  }
  coordsStr += "};\n";
  coordsStr += QString("for (var c in lst%1) {\n"
               "  var circle = new google.maps.Circle({\n"
               "  strokeColor: '%2',\n"
               "  strokeOpacity: 0.8,\n"
               "  strokeWeight: 2,\n"
               "  fillColor: '%2',\n"
               "  fillOpacity: 0.35,\n"
               "  map: map,\n"
               "  center: lst%1[c].center,\n"
               "  radius: 10\n"
               "});\n").arg(prefix).arg(color);
  coordsStr += "}\n";
  return coordsStr;
}

static double filterDistanceRealTime(const std::vector<geopoint_t> &lst,
                                     int prec,
                                     int minPointCount) {
  static const double COORD_NOT_INITIALIZED = 361.0;

  if (lst.empty() || lst.size() == 1)
    return 0.0;  

  double distance = 0.0;
  int count;
  geopoint_t tmpGeo, laGeo;
  auto pi = lst.begin();
  uint64_t gh0, gh;
  uint64_t *tgh0, *tgh;
  tgh0 = &gh0;
  tgh = &gh;

  laGeo.Latitude = laGeo.Longitude = COORD_NOT_INITIALIZED;

  *tgh0 = GeohashEncodeU64(pi->Latitude, pi->Longitude, prec);
  tmpGeo.Latitude = pi->Latitude;
  tmpGeo.Longitude = pi->Longitude;
  count = 1;

  for (++pi; pi != lst.end(); ++pi) {
     *tgh = GeohashEncodeU64(pi->Latitude, pi->Longitude, prec);

    //if (ppCompGeohash != ppReadGeohash)
    if (*tgh - *tgh0) {
      if (count >= minPointCount) {
        tmpGeo.Latitude /= count;
        tmpGeo.Longitude /= count;

        if (laGeo.Latitude != COORD_NOT_INITIALIZED) {
          double dd = CoordDistanceBetweenPointsMeters(laGeo.Latitude, laGeo.Longitude,
                                                       tmpGeo.Latitude, tmpGeo.Longitude);
          distance += dd;
        }
        laGeo = tmpGeo;
        tmpGeo.Latitude = tmpGeo.Longitude = 0.0;
      }

      count = 1;
      tmpGeo.Latitude = pi->Latitude;
      tmpGeo.Longitude = pi->Longitude;
      std::swap(*tgh0, *tgh);
      continue;
    }

    tmpGeo.Latitude += pi->Latitude;
    tmpGeo.Longitude += pi->Longitude;
    ++count;
  }

  //last coordinates need to be used
  if (count >= minPointCount) {
    tmpGeo.Latitude /= count;
    tmpGeo.Longitude /= count;

    if (laGeo.Latitude != COORD_NOT_INITIALIZED) {
      double dd = CoordDistanceBetweenPointsMeters(laGeo.Latitude, laGeo.Longitude, tmpGeo.Latitude, tmpGeo.Longitude);
      distance += dd;
    }
  }

  return distance;
}

void
MainWindow::initMap(QWebEnginePage *page,
                    const QString &pathToCoordsFile,
                    const QString &filteredCoordsFile,
                    const QString &filteredCoordsFile2) {
  (void)filteredCoordsFile2;
  std::vector<geopoint_t> lstCoords = CoordGetFromFile(pathToCoordsFile, LMT_GPS_DATA);
  std::vector<geopoint_t> lstGeoFilter = CoordGetFromFile(filteredCoordsFile, LMT_FILTERED_GPS_DATA);
  std::vector<geopoint_t> lstJavaFilter = CoordGetFromFile(pathToCoordsFile, LMT_FILTERED_GPS_DATA);
//  std::vector<geopoint_t> lstJavaFilter = CoordGetFromFile(filteredCoordsFile2, LMT_FILTERED_GPS_DATA);
  const int filterPrec = 6;
  const int minPoints = 3;

  qDebug() << "Src distance (OLD filter or GPS): " << filterDistanceRealTime(lstCoords, filterPrec, minPoints);
  qDebug() << "Desktop distance: " << filterDistanceRealTime(lstGeoFilter, filterPrec, minPoints);
  qDebug() << "New filter or java logged distance: " << filterDistanceRealTime(lstJavaFilter, filterPrec, minPoints);

  qDebug() << "Src distance : " << CoordCaclulateDistance(lstCoords);
  qDebug() << "Filtered distance : " << CoordCaclulateDistance(lstGeoFilter);
  qDebug() << "Java distance : " << CoordCaclulateDistance(lstJavaFilter);

  //filter for display
  lstCoords = CoordFilterByGeoHash(lstCoords, filterPrec, minPoints);
  lstJavaFilter = CoordFilterByGeoHash(lstJavaFilter, filterPrec, minPoints);
  lstGeoFilter = CoordFilterByGeoHash(lstGeoFilter, filterPrec, minPoints);

  qDebug() << "2Src distance : " << CoordCaclulateDistance(lstCoords);
  qDebug() << "2Filtered distance : " << CoordCaclulateDistance(lstGeoFilter);
  qDebug() << "2Java distance : " << CoordCaclulateDistance(lstJavaFilter);

  QString srcCoordsStr = jsCoordsString(lstCoords, "src", "#FF0000");
  QString geoCoordsStr = jsCoordsString(lstGeoFilter, "geo", "#0000FF");
  QString javaCoordsStr = jsCoordsString(lstJavaFilter, "jkf", "#007700");
  QString allCoordsStr = srcCoordsStr + geoCoordsStr + javaCoordsStr;

  double lat, lon;
  lat = lstCoords.empty() ? 42.87336 : lstCoords[0].Latitude;
  lon = lstCoords.empty() ? 74.61873 : lstCoords[0].Longitude;
  QString html = g_baseHtml.arg(g_mapDiv).arg(allCoordsStr).arg(lat).arg(lon);
  page->setHtml(html);

  if (1) {
    QFile sf("/home/lezh1k/route.html");
    if (sf.open(QFile::ReadWrite)) {
      sf.write(html.toUtf8());
      sf.close();
    }
  }
}
