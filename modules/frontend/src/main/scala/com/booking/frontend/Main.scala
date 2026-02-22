package com.booking.frontend

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import com.booking.frontend.pages.*

object Main:
  def main(args: Array[String]): Unit =
    renderOnDomContentLoaded(
      dom.document.getElementById("app"),
      appElement(),
    )

  private val currentPath: Var[String] = Var(dom.window.location.pathname)

  def navigateTo(path: String): Unit =
    dom.window.history.pushState(null, "", path)
    currentPath.set(path)

  def appElement(): Element =
    div(
      cls := "app",
      headerElement(),
      mainElement(),
    )

  def headerElement(): Element =
    headerTag(
      cls := "app-header",
      div(
        cls := "header-content",
        a(
          href := "/",
          cls := "logo",
          "🧘 PilatesBook",
          onClick.preventDefault --> { _ => navigateTo("/") },
        ),
        navTag(
          cls := "nav",
          a(
            href := "/",
            "Schedule",
            onClick.preventDefault --> { _ => navigateTo("/") },
          ),
          a(
            href := "/profile",
            "My Bookings",
            onClick.preventDefault --> { _ => navigateTo("/profile") },
          ),
        ),
      ),
    )

  def mainElement(): Element =
    mainTag(
      cls := "app-main",
      child <-- currentPath.signal.map { path =>
        if path == "/profile" then ProfilePage.render()
        else if path.startsWith("/book/") then BookingPage.render(path.stripPrefix("/book/"))
        else SchedulePage.render()
      },
    )
