package com.bogab.akkrawler

import com.mongodb.casbah.Imports._

object MongoDumper {

  def dump(page: Page): Unit = {
    val mongoPage = buildMongoDbPage(page)
    MongoFactory.collection.save(mongoPage)
  }

  def buildMongoDbPage(page: Page): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "_id" -> page.url.hashCode
    builder += "url" -> page.url
    builder += "title" -> page.title
    builder += "description" -> page.description
    builder += "keywords" -> page.keywords
    builder += "robots" -> page.robots
    builder += "text" -> page.text
    builder += "links" -> page.links

    builder.result()
  }
}
