package com.bogab.akkrawler

class Page {
  var url = ""
  var text = ""
  var title = ""
  var robots = ""
  var keywords = ""
  var description = ""
  var links: Set[String] = Set()
}
