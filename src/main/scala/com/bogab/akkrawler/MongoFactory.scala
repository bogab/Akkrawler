package com.bogab.akkrawler

import com.mongodb.casbah.{MongoClient, MongoCollection}

object MongoFactory {
  private val SERVER = "localhost"
  private val PORT = 27017
  private val DATABASE = "akkrawler"
  private val COLLECTION = "pages"
  val connection: MongoClient = MongoClient(SERVER)
  val collection: MongoCollection = connection(DATABASE)(COLLECTION)
}
