package bookingplatform.ui.pages

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import bookingplatform.ui.api.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

// ─── Sessions Page ────────────────────
object SessionsPage:
  def render(currentUserId: Signal[Option[String]]): HtmlElement =
    val sessions = Var(List.empty[SessionDto])
    val scheduledSessions = Var(List.empty[ScheduledSessionDto])
    val error = Var(Option.empty[String])
    val loading = Var(false)

    def loadData(): Unit =
      loading.set(true)
      ApiClient.getSessions().onComplete {
        case Success(s) => sessions.set(s); loading.set(false)
        case Failure(e) => error.set(Some(e.getMessage)); loading.set(false)
      }
      ApiClient.getUpcomingScheduled().onComplete {
        case Success(s) => scheduledSessions.set(s)
        case Failure(e) => error.set(Some(e.getMessage))
      }

    div(
      cls := "page",
      onMountCallback(_ => loadData()),
      h2("Available Sessions"),
      child <-- error.signal.map {
        case Some(msg) => div(cls := "error", s"Error: $msg")
        case None      => emptyNode
      },
      child <-- loading.signal.map(l => if l then div(cls := "loading", "Loading...") else emptyNode),
      div(
        cls := "card-grid",
        children <-- sessions.signal.map(_.map { session =>
          div(
            cls := "card",
            h3(session.name),
            p(s"Type: ${session.sessionType}"),
            p(s"Duration: ${session.durationMinutes} min"),
            p(s"Price: $$${session.price}"),
            p(s"Capacity: ${session.capacity}"),
            session.stationCount.map(sc => p(s"Stations: $sc")).getOrElse(emptyNode),
            session.description.map(d => p(cls := "description", d)).getOrElse(emptyNode)
          )
        })
      ),
      h2("Upcoming Scheduled Sessions"),
      div(
        cls := "card-grid",
        children <-- scheduledSessions.signal.combineWith(currentUserId).map { case (scheduled, userId) =>
          scheduled.map { ss =>
            div(
              cls := "card",
              h4(s"Session: ${ss.sessionId.take(8)}..."),
              p(s"Start: ${ss.startTime}"),
              p(s"End: ${ss.endTime}"),
              p(s"Bookings: ${ss.currentBookings}"),
              p(s"Status: ${ss.status}"),
              userId.map { uid =>
                button(
                  cls := "btn btn-primary",
                  "Book Now",
                  onClick --> { _ =>
                    ApiClient.createBooking(CreateBookingReq(uid, ss.id, None)).onComplete {
                      case Success(b) =>
                        org.scalajs.dom.window.alert(s"Booked! Status: ${b.status}")
                        loadData()
                      case Failure(e) =>
                        org.scalajs.dom.window.alert(s"Booking failed: ${e.getMessage}")
                    }
                  }
                )
              }.getOrElse(p(cls := "hint", "Login to book"))
            )
          }
        }
      )
    )

// ─── Bookings Page ────────────────────
object BookingsPage:
  def render(currentUserId: Signal[Option[String]]): HtmlElement =
    val bookings = Var(List.empty[BookingDto])
    val error = Var(Option.empty[String])

    div(
      cls := "page",
      h2("My Bookings"),
      child <-- currentUserId.map {
        case None => p("Please select a user to view bookings.")
        case Some(uid) =>
          val _ = ApiClient.getUserBookings(uid).onComplete {
            case Success(b) => bookings.set(b)
            case Failure(e) => error.set(Some(e.getMessage))
          }
          div(
            child <-- error.signal.map {
              case Some(msg) => div(cls := "error", s"Error: $msg")
              case None      => emptyNode
            },
            div(
              cls := "card-grid",
              children <-- bookings.signal.map(_.map { booking =>
                div(
                  cls := "card",
                  h4(s"Booking ${booking.id.take(8)}..."),
                  p(s"Session: ${booking.scheduledSessionId.take(8)}..."),
                  p(s"Status: ${booking.status}"),
                  p(s"Booked: ${booking.createdAt}"),
                  booking.subscriptionId.map(s => p(s"Subscription: ${s.take(8)}...")).getOrElse(emptyNode),
                  if booking.status == "Confirmed" then
                    button(
                      cls := "btn btn-danger",
                      "Cancel Booking",
                      onClick --> { _ =>
                        ApiClient.cancelBooking(booking.id, CancelBookingReq(Some("User cancelled"))).onComplete {
                          case Success(c) =>
                            org.scalajs.dom.window.alert(s"Cancelled. Late fee: $$${c.lateFee}")
                            ApiClient.getUserBookings(uid).onComplete {
                              case Success(b) => bookings.set(b)
                              case Failure(_) => ()
                            }
                          case Failure(e) =>
                            org.scalajs.dom.window.alert(s"Cancel failed: ${e.getMessage}")
                        }
                      }
                    )
                  else emptyNode
                )
              })
            )
          )
      }
    )

// ─── Subscriptions Page ────────────────────
object SubscriptionsPage:
  def render(currentUserId: Signal[Option[String]]): HtmlElement =
    val plans = Var(List.empty[SubscriptionPlanDto])
    val userSubs = Var(List.empty[SubscriptionDto])
    val error = Var(Option.empty[String])

    def loadPlans(): Unit =
      ApiClient.getSubscriptionPlans().onComplete {
        case Success(p) => plans.set(p)
        case Failure(e) => error.set(Some(e.getMessage))
      }

    def loadUserSubs(uid: String): Unit =
      ApiClient.getUserSubscriptions(uid).onComplete {
        case Success(s) => userSubs.set(s)
        case Failure(e) => error.set(Some(e.getMessage))
      }

    div(
      cls := "page",
      onMountCallback(_ => loadPlans()),
      h2("Subscription Plans"),
      child <-- error.signal.map {
        case Some(msg) => div(cls := "error", s"Error: $msg")
        case None      => emptyNode
      },
      div(
        cls := "card-grid",
        children <-- plans.signal.combineWith(currentUserId).map { case (planList, userId) =>
          planList.map { plan =>
            div(
              cls := "card",
              h3(plan.name),
              p(s"Type: ${plan.planType}"),
              p(s"Price: $$${plan.price}"),
              plan.sessionCount.map(sc => p(s"Sessions: $sc")).getOrElse(p("Unlimited sessions")),
              plan.validityDays.map(d => p(s"Valid for: $d days")).getOrElse(emptyNode),
              plan.description.map(d => p(cls := "description", d)).getOrElse(emptyNode),
              userId.map { uid =>
                button(
                  cls := "btn btn-primary",
                  "Subscribe",
                  onClick --> { _ =>
                    ApiClient.subscribe(CreateSubscriptionReq(uid, plan.id)).onComplete {
                      case Success(s) =>
                        org.scalajs.dom.window.alert(s"Subscribed! Status: ${s.status}")
                        loadUserSubs(uid)
                      case Failure(e) =>
                        org.scalajs.dom.window.alert(s"Subscribe failed: ${e.getMessage}")
                    }
                  }
                )
              }.getOrElse(emptyNode)
            )
          }
        }
      ),
      h2("My Subscriptions"),
      child <-- currentUserId.map {
        case None => p("Please select a user to view subscriptions.")
        case Some(uid) =>
          val _ = loadUserSubs(uid)
          div(
            cls := "card-grid",
            children <-- userSubs.signal.map(_.map { sub =>
              div(
                cls := "card",
                h4(s"Subscription ${sub.id.take(8)}..."),
                p(s"Plan: ${sub.planId.take(8)}..."),
                p(s"Status: ${sub.status}"),
                p(s"Start: ${sub.startDate}"),
                sub.endDate.map(d => p(s"End: $d")).getOrElse(emptyNode),
                sub.remainingSessions.map(r => p(s"Remaining: $r sessions")).getOrElse(p("Unlimited")),
              )
            })
          )
      }
    )

// ─── User Management Page ────────────────────
object UsersPage:
  def render(currentUserId: Var[Option[String]]): HtmlElement =
    val users = Var(List.empty[UserDto])
    val nameInput = Var("")
    val emailInput = Var("")
    val phoneInput = Var("")
    val error = Var(Option.empty[String])

    def loadUsers(): Unit =
      ApiClient.getUsers().onComplete {
        case Success(u) => users.set(u)
        case Failure(e) => error.set(Some(e.getMessage))
      }

    div(
      cls := "page",
      onMountCallback(_ => loadUsers()),
      h2("Users"),
      child <-- error.signal.map {
        case Some(msg) => div(cls := "error", s"Error: $msg")
        case None      => emptyNode
      },
      div(
        cls := "form",
        h3("Create New User"),
        div(
          label("Name: "),
          input(
            typ := "text",
            placeholder := "Full name",
            controlled(value <-- nameInput.signal, onInput.mapToValue --> nameInput)
          )
        ),
        div(
          label("Email: "),
          input(
            typ := "email",
            placeholder := "email@example.com",
            controlled(value <-- emailInput.signal, onInput.mapToValue --> emailInput)
          )
        ),
        div(
          label("Phone: "),
          input(
            typ := "tel",
            placeholder := "+1234567890",
            controlled(value <-- phoneInput.signal, onInput.mapToValue --> phoneInput)
          )
        ),
        button(
          cls := "btn btn-primary",
          "Create User",
          onClick --> { _ =>
            val phone = if phoneInput.now().isEmpty then None else Some(phoneInput.now())
            ApiClient.createUser(CreateUserReq(nameInput.now(), emailInput.now(), phone)).onComplete {
              case Success(_) =>
                nameInput.set(""); emailInput.set(""); phoneInput.set("")
                loadUsers()
              case Failure(e) => error.set(Some(e.getMessage))
            }
          }
        )
      ),
      h3("Select Active User"),
      div(
        cls := "card-grid",
        children <-- users.signal.combineWith(currentUserId.signal).map { case (userList, selectedId) =>
          userList.map { user =>
            div(
              cls := s"card ${if selectedId.contains(user.id) then "card-selected" else ""}",
              h4(user.name),
              p(user.email),
              user.phone.map(ph => L.p(s"Phone: $ph")).getOrElse(emptyNode),
              button(
                cls := s"btn ${if selectedId.contains(user.id) then "btn-secondary" else "btn-primary"}",
                if selectedId.contains(user.id) then "Selected ✓" else "Select",
                onClick --> { _ => currentUserId.set(Some(user.id)) }
              )
            )
          }
        }
      )
    )

