package com.booking.frontend.pages

import com.raquo.laminar.api.L.*
import com.booking.frontend.AppState
import com.booking.frontend.components.ClassCard
import com.booking.frontend.model.*

object SchedulePage:
  def render(): Element =
    div(
      cls := "page schedule-page",
      h1("Class Schedule"),
      p(cls := "subtitle", "Book your next pilates or fitness class"),
      div(
        cls := "schedule-filters",
        button(cls := "btn btn-outline", "This Week"),
        button(cls := "btn btn-outline", "Next Week"),
      ),
      child <-- AppState.isLoading.signal.map { loading =>
        if loading then
          div(cls := "loading", "Loading sessions...")
        else
          div(
            cls := "sessions-grid",
            children <-- AppState.sessions.signal.map(_.map(ClassCard.render)),
          )
      },
      child <-- AppState.errorMessage.signal.map {
        case Some(msg) => div(cls := "error-message", msg)
        case None      => emptyNode
      },
    )
