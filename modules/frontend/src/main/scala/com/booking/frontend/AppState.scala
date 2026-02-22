package com.booking.frontend

import com.raquo.laminar.api.L.*
import com.booking.frontend.model.*

object AppState:
  val currentMemberId: Var[Option[String]] = Var(None)
  val sessions: Var[List[ClassSessionView]] = Var(List.empty)
  val myBookings: Var[List[BookingView]] = Var(List.empty)
  val isLoading: Var[Boolean] = Var(false)
  val errorMessage: Var[Option[String]] = Var(None)
