package com.booking.frontend

import com.raquo.laminar.api.L.*
import io.frontroute.*
import org.scalajs.dom
import com.booking.frontend.pages.*

object Main:
  def main(args: Array[String]): Unit =
    renderOnDomContentLoaded(
      dom.document.getElementById("app"),
      appElement(),
    )

  def appElement(): Element =
    div(
      cls := "app",
      headerElement(),
      mainElement(),
    )

  def headerElement(): Element =
    header(
      cls := "app-header",
      div(
        cls := "header-content",
        a(href := "/", cls := "logo", "🧘 PilatesBook"),
        nav(
          cls := "nav",
          a(href := "/", "Schedule"),
          a(href := "/profile", "My Bookings"),
        ),
      ),
    )

  def mainElement(): Element =
    main(
      cls := "app-main",
      pathSwitch(
        path(segment("profile"))(ProfilePage.render()),
        path(segment("book") / segment)(sessionId => BookingPage.render(sessionId)),
        pathEnd(SchedulePage.render()),
      ),
    )
