#ifndef MEANFILTER_H
#define MEANFILTER_H

//todo rewrite using C style

#include <stdint.h>
#include <vector>
#include <deque>
#include <assert.h>
#include <math.h>

static const double DEFAULT_TIME_WINDOW = 0.2;
template<typename T> class CMeanFilter {
private:

  uint32_t m_startTimeUs;
  uint32_t m_timeStampUs;
  uint32_t m_count;
  double   m_timeWindow;
  std::deque<std::vector<T> > m_deckWindow;

  void mean(std::vector<T> &meanBuff) const {
    for (auto i = m_deckWindow.cbegin(); i != m_deckWindow.cend(); ++i) {
      auto j1 = i->cbegin();
      auto j2 = meanBuff.begin();
      for (; j1 != i->cend(); ++j1, ++j2) {
        *j2 += *j1;
      }
    }

    for (auto j2 = meanBuff.begin(); j2 != meanBuff.end(); ++j2) {
      *j2 /= meanBuff.size();
    }
  }

public:

  CMeanFilter() :
    m_startTimeUs(0),
    m_timeStampUs(0),
    m_count(0),
    m_timeWindow(DEFAULT_TIME_WINDOW) {

  }

  void filter(T val,
              std::vector<T> &meanBuff,
              uint32_t timeStampUs) {
    std::vector<T> tmp;
    tmp.push_back(val);
    filterArr(tmp, meanBuff, timeStampUs);
  }

  void filterArr(const std::vector<T> &data,
              std::vector<T> &meanBuff,
              uint32_t timeStampUs) {
    assert(data.size() == meanBuff.size());
    m_timeStampUs = timeStampUs;
    ++m_count;
    if (m_startTimeUs == 0UL) {
      m_startTimeUs = timeStampUs;
      auto i = meanBuff.begin();
      auto j = data.cbegin();
      for (; i != meanBuff.end(); ++i, ++j)
        *i = *j;      
      return;
    }

    double hz = m_count / ((m_timeStampUs - m_startTimeUs) / 1.0e3); //herz frequency
    uint32_t fw = ceil(hz * m_timeWindow); //filter window
    m_deckWindow.push_back(data);
    while (m_deckWindow.size() > fw)
      m_deckWindow.pop_front();
    mean(meanBuff);
  }
};

#endif // MEANFILTER_H
