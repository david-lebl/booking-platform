package com.booking.frontend.components

import com.raquo.laminar.api.L.*
import com.booking.frontend.model.ClassSessionView

object ClassCard:
  def render(session: ClassSessionView): Element =
    div(
      cls := "class-card",
      div(
        cls := "class-card-header",
        h3(cls := "class-name", session.classTypeName),
        span(
          cls := s"availability ${if session.availableSpots > 0 then "available" else "full"}",
          if session.availableSpots > 0 then s"${session.availableSpots} spots left" else "Full",
        ),
      ),
      div(
        cls := "class-card-body",
        div(cls := "class-detail", span(cls := "icon", "👤"), session.instructorName),
        div(cls := "class-detail", span(cls := "icon", "📍"), session.studioName),
        div(cls := "class-detail", span(cls := "icon", "🕐"), session.startTime),
        div(
          cls := "class-detail capacity",
          span(cls := "icon", "🪑"),
          s"${session.maxCapacity - session.availableSpots}/${session.maxCapacity}",
        ),
      ),
      div(
        cls := "class-card-footer",
        if session.availableSpots > 0 then
          a(
            href := s"/book/${session.id}",
            cls  := "btn btn-primary",
            "Book Now",
          )
        else
          button(cls := "btn btn-disabled", disabled := true, "Class Full"),
      ),
    )
