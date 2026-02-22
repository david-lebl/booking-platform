package com.bookingplatform.ui

import com.raquo.laminar.api.L.{*, given}
import com.raquo.waypoint.*
import zio.json.*

sealed trait Page derives JsonEncoder, JsonDecoder
object Page:
  case object Home       extends Page derives JsonEncoder, JsonDecoder
  case object Schedule   extends Page derives JsonEncoder, JsonDecoder
  case object MyBookings extends Page derives JsonEncoder, JsonDecoder
  case object Login      extends Page derives JsonEncoder, JsonDecoder

object Router:

  private val homeRoute     = Route.static(Page.Home, root / endOfSegments)
  private val scheduleRoute = Route.static(Page.Schedule, root / "schedule" / endOfSegments)
  private val bookingsRoute = Route.static(Page.MyBookings, root / "my-bookings" / endOfSegments)
  private val loginRoute    = Route.static(Page.Login, root / "login" / endOfSegments)

  private val currentPage: Var[Page] = Var(Page.Home)

  val renderPage: HtmlElement = div(
    child <-- currentPage.signal.map {
      case Page.Home       => homePage
      case Page.Schedule   => schedulePage
      case Page.MyBookings => myBookingsPage
      case Page.Login      => loginPage
    }
  )

  private def homePage = div(
    h2(cls := "text-2xl font-semibold mb-4", "Home"),
    p("Browse available classes and book your session."),
    div(
      cls := "mt-4 space-x-4",
      button(
        cls := "text-blue-600 hover:underline",
        "View Schedule",
        onClick --> { _ => currentPage.set(Page.Schedule) }
      ),
      button(
        cls := "text-blue-600 hover:underline",
        "My Bookings",
        onClick --> { _ => currentPage.set(Page.MyBookings) }
      )
    )
  )

  private def schedulePage = div(
    h2(cls := "text-2xl font-semibold mb-4", "Weekly Schedule"),
    p("Schedule view coming soon..."),
    button(cls := "text-blue-600 hover:underline mt-4", "Back to Home",
      onClick --> { _ => currentPage.set(Page.Home) })
  )

  private def myBookingsPage = div(
    h2(cls := "text-2xl font-semibold mb-4", "My Bookings"),
    p("Your bookings will appear here."),
    button(cls := "text-blue-600 hover:underline mt-4", "Back to Home",
      onClick --> { _ => currentPage.set(Page.Home) })
  )

  private def loginPage = div(
    h2(cls := "text-2xl font-semibold mb-4", "Login"),
    p("Login form coming soon..."),
    button(cls := "text-blue-600 hover:underline mt-4", "Back to Home",
      onClick --> { _ => currentPage.set(Page.Home) })
  )
