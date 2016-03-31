package com.sam.routes

import java.util.concurrent.TimeUnit
import akka.actor.{ActorRef, ActorSystem}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.util.Timeout
import com.sam.comm._
import com.typesafe.config.Config
import scala.concurrent.{ExecutionContext, Future}

trait PostsOperations extends Directives with JsonSupport {
  implicit val system: ActorSystem

  implicit val executor: ExecutionContext

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter

  implicit def postsSender: ActorRef

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  lazy val postsRoutes = {
    getRoute ~ postRoute
  }

  private val getRoute =
    path("api" / "v1" / "posts" / Segment) { postid =>
      get {
        logRequestResult("posts-get") {
          val reply: Future[Any] = postsSender ? PostsGetJob(postid)
          onSuccess(reply) { x =>
            x match {
              case Right(posts: Posts) => complete(StatusCodes.OK, posts)
              case Left(msg: String) => complete(StatusCodes.InternalServerError, Map("msg" -> msg))
              case JobFailed(msg: String) => complete(StatusCodes.InternalServerError, Map("msg" -> msg))
            }
          }
        }
      }
    }

  private val postRoute =
    path("api" / "v1" / "posts") {
      post {
        logRequestResult("posts-post") {
          entity(as[Posts]) { posts =>
            val reply: Future[Any] = postsSender ? PostsCreateJob(posts)
            onSuccess(reply) { x =>
              x match {
                case Right(postid: String) => complete(StatusCodes.Created, Map("postid" -> postid))
                case Left(msg: String) => complete(StatusCodes.InternalServerError, Map("msg" -> msg))
                case JobFailed(msg: String) => complete(StatusCodes.InternalServerError, Map("msg" -> msg))
              }
            }
          }
        }
      }
    }
}
