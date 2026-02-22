package com.bookingplatform.ui

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object App:
  def main(args: Array[String]): Unit =
    val app = div(
      cls := "min-h-screen bg-gray-50",
      div(
        cls := "container mx-auto px-4 py-8",
        h1(cls := "text-3xl font-bold text-gray-900 mb-4", "Booking Platform"),
        p(cls := "text-gray-600", "Welcome to the Fitness Booking Platform"),
        Router.renderPage
      )
    )
    render(dom.document.getElementById("app"), app)
