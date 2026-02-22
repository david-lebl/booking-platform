package com.booking.frontend.api

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import org.scalajs.dom.XMLHttpRequest
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object ApiClient:

  private val baseUrl = "/api"

  def get(path: String): Future[String] =
    val promise = Promise[String]()
    val xhr = new XMLHttpRequest()
    xhr.open("GET", s"$baseUrl$path")
    xhr.setRequestHeader("Content-Type", "application/json")
    xhr.onload = _ =>
      if xhr.status >= 200 && xhr.status < 300 then
        promise.success(xhr.responseText)
      else
        promise.failure(new RuntimeException(s"HTTP ${xhr.status}: ${xhr.responseText}"))
    xhr.onerror = _ => promise.failure(new RuntimeException("Network error"))
    xhr.send()
    promise.future

  def post(path: String, body: String): Future[String] =
    val promise = Promise[String]()
    val xhr = new XMLHttpRequest()
    xhr.open("POST", s"$baseUrl$path")
    xhr.setRequestHeader("Content-Type", "application/json")
    xhr.onload = _ =>
      if xhr.status >= 200 && xhr.status < 300 then
        promise.success(xhr.responseText)
      else
        promise.failure(new RuntimeException(s"HTTP ${xhr.status}: ${xhr.responseText}"))
    xhr.onerror = _ => promise.failure(new RuntimeException("Network error"))
    xhr.send(body)
    promise.future

  def delete(path: String): Future[String] =
    val promise = Promise[String]()
    val xhr = new XMLHttpRequest()
    xhr.open("DELETE", s"$baseUrl$path")
    xhr.setRequestHeader("Content-Type", "application/json")
    xhr.onload = _ =>
      if xhr.status >= 200 && xhr.status < 300 then
        promise.success(xhr.responseText)
      else
        promise.failure(new RuntimeException(s"HTTP ${xhr.status}: ${xhr.responseText}"))
    xhr.onerror = _ => promise.failure(new RuntimeException("Network error"))
    xhr.send()
    promise.future
