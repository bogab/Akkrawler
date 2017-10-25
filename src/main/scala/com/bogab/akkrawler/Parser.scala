package com.bogab.akkrawler

import akka.actor.Actor
import okhttp3.Response
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex
import scala.util.{Failure, Success}

class Parser(url: String) extends Actor {

  private var pageDocument: Document = new Document("")
  private val validator = new UrlValidator()

  Fetcher.fetch(url).onComplete {
    case Success(response) => self ! Success(response)
    case Failure(exception) => self ! Failure(exception)
  }

  override def receive = {
    case Failure(e) =>
      println(s"Error fetching: $url aborting --> $e")
      context.parent ! ParseComplete(url, Set.empty[String])
    case Success(response: Response) =>
      val pageData = parsePage(response.body().string())
      MongoDumper.dump(pageData)
      context.parent ! ParseComplete(url, pageData.links)
  }

  def parsePage(data: String): Page = {
    println(s"Parsing: $url")
    pageDocument = Jsoup.parse(data)
    val pageData = new Page()
    val noIndex: Regex = "(none|noindex)".r
    val noFollow: Regex = "(none|nofollow)".r
    pageData.url = url
    pageData.title = pageDocument.title
    pageData.robots = getMetaTag("robots")
    pageData.keywords = getMetaTag("keywords")
    pageData.description = getMetaTag("description")

    noIndex.findFirstIn(pageData.robots) match {
      case Some(_) => pageData.text = ""
      case None => pageData.text = getText
    }

    noFollow.findFirstIn(pageData.robots) match {
      case Some(_) => pageData.links = Set.empty[String]
      case None => pageData.links = pageDocument.getElementsByTag("a").asScala.map(e => e.attr("href")).filter(s => validator.isValid(s)).toSet
    }

    pageData
  }

  private def getMetaTag(attr: String): String = {
    pageDocument.select("meta[name=" + attr + "]").toString
  }

  private def getText = {
    var text = pageDocument.text()
    text += pageDocument.getElementsByTag("img").asScala.map(e => e.attr("alt")) mkString "\n"
    text += pageDocument.getElementsByTag("a").asScala.map(e => e.attr("title")).filter(s => !s.replaceAll("\\s", "").isEmpty) mkString "\n"

    text
  }
}

