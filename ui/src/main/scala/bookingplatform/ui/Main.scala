package bookingplatform.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import bookingplatform.ui.pages.*

object Main:
  // Current page state
  enum Page:
    case Sessions, Bookings, Subscriptions, Users

  val currentPage = Var(Page.Users)
  val currentUserId = Var(Option.empty[String])

  def main(args: Array[String]): Unit =
    val appContainer = dom.document.getElementById("app")
    val appElement = div(
      cls := "app",
      // Header
      headerTag(
        cls := "header",
        h1("🏋️ Booking Platform"),
        htmlTag("nav")(
          cls := "nav",
          navButton("Users", Page.Users),
          navButton("Sessions", Page.Sessions),
          navButton("My Bookings", Page.Bookings),
          navButton("Subscriptions", Page.Subscriptions)
        ),
        child <-- currentUserId.signal.map {
          case Some(id) => span(cls := "user-badge", s"Active User: ${id.take(8)}...")
          case None     => span(cls := "user-badge warning", "No user selected")
        }
      ),
      // Main content
      mainTag(
        cls := "main-content",
        child <-- currentPage.signal.map {
          case Page.Sessions      => SessionsPage.render(currentUserId.signal)
          case Page.Bookings      => BookingsPage.render(currentUserId.signal)
          case Page.Subscriptions => SubscriptionsPage.render(currentUserId.signal)
          case Page.Users         => UsersPage.render(currentUserId)
        }
      ),
      // Footer
      footerTag(
        cls := "footer",
        p("Booking Platform © 2024 — Powered by Scala 3, ZIO, Tapir & Laminar")
      )
    )
    render(appContainer, appElement)

  private def navButton(label: String, page: Page): HtmlElement =
    button(
      cls <-- currentPage.signal.map(p => s"nav-btn ${if p == page then "active" else ""}"),
      label,
      onClick --> { _ => currentPage.set(page) }
    )
