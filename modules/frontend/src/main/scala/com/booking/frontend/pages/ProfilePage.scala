package com.booking.frontend.pages

import com.raquo.laminar.api.L.*
import com.booking.frontend.AppState

object ProfilePage:
  def render(): Element =
    div(
      cls := "page profile-page",
      h1("My Bookings"),
      child <-- AppState.myBookings.signal.map { bookings =>
        if bookings.isEmpty then
          div(
            cls := "empty-state",
            p("No bookings yet."),
            a(href := "/", cls := "btn btn-primary", "Browse Classes"),
          )
        else
          div(
            cls := "bookings-list",
            bookings.map { booking =>
              div(
                cls := "booking-item",
                div(cls := "booking-session", s"Session: ${booking.classSessionId}"),
                div(cls := "booking-time", booking.startTime),
                div(
                  cls := s"booking-status status-${booking.status.toLowerCase}",
                  booking.status,
                ),
              )
            },
          )
      },
    )
