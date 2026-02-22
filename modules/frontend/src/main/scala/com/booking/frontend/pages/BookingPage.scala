package com.booking.frontend.pages

import com.raquo.laminar.api.L.*
import com.booking.frontend.AppState

object BookingPage:
  def render(sessionId: String): Element =
    val memberIdVar = Var("")
    val statusVar   = Var[Option[String]](None)

    div(
      cls := "page booking-page",
      h1("Book a Class"),
      div(
        cls := "booking-form",
        div(
          cls := "form-group",
          label(forId := "memberId", "Member ID"),
          input(
            idAttr      := "memberId",
            cls         := "form-input",
            typ         := "text",
            placeholder := "Enter your member ID",
            onInput.mapToValue --> memberIdVar,
          ),
        ),
        p(cls := "session-info", s"Session: $sessionId"),
        button(
          cls := "btn btn-primary",
          typ := "button",
          "Confirm Booking",
          onClick --> { _ =>
            val memberId = memberIdVar.now()
            if memberId.nonEmpty then
              statusVar.set(Some("Booking confirmed! 🎉"))
            else
              statusVar.set(Some("Please enter your member ID"))
          },
        ),
        child <-- statusVar.signal.map {
          case Some(msg) => p(cls := "status-message", msg)
          case None      => emptyNode
        },
      ),
    )
