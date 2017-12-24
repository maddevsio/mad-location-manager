#include "mainwindow.h"
#include <QMessageBox>
#include <QWebEngineProfile>
#include <Coordinates.h>

static const QString g_mapDiv = "megamap";
static const QString g_baseHtml = "<!DOCTYPE html>\n"
                                  "<html>\n"
                                  "<head>\n"
                                  "  <style>\n"
                                  "     #%1 {\n"
                                  "      height: 600px;\n"
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
                                  "    }\n"
                                  "  </script>\n"
                                  "  <script async defer\n"
                                  "  src=\"https://maps.googleapis.com/maps/api/js?callback=initMap\">\n"
                                  "  </script>\n"
                                  "</body>\n"
                                  "</html>\n";

MainWindow::MainWindow(const QString &coordsFilePath,
                       const QString &coordsFilePath2,
                       QWidget *parent)
  : QMainWindow(parent),
    m_view(new QWebEngineView(this)),
    m_coordsFilePath(coordsFilePath),
    m_coordsFilePath2(coordsFilePath2)
{
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

  initMap(page, m_coordsFilePath, m_coordsFilePath2);
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

void
MainWindow::initMap(QWebEnginePage *page,
                    const QString &pathToCoordsFile,
                    const QString &pathToCoordsFile2) {
  std::vector<geopoint_t> lstCoords = GetCoordsFromFile(pathToCoordsFile);
  lstCoords = FilterByGeoHash(lstCoords, 9, 2);
//  std::vector<geopoint_t> lstGeoFilter = FilterByGeoHash(lstCoords, 7, 2);
  std::vector<geopoint_t> lstGeoFilter = GetCoordsFromFile(pathToCoordsFile2);
  lstGeoFilter = FilterByGeoHash(lstGeoFilter, 9, 2);
  QString srcCoordsStr = jsCoordsString(lstCoords, "src", "#FF0000");
  QString geoCoordsStr = jsCoordsString(lstGeoFilter, "geo", "#0000FF");
  QString allCoordsStr = srcCoordsStr + geoCoordsStr;

  double coord00, coord01;
  coord00 = lstCoords.empty() ? 42.87336 : lstCoords[0].Latitude;
  coord01 = lstCoords.empty() ? 74.61873 : lstCoords[0].Longitude;
  QString html = g_baseHtml.arg(g_mapDiv).arg(allCoordsStr).arg(coord00).arg(coord01);
  page->setHtml(html);
}
