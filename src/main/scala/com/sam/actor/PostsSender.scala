package com.sam.actor

import akka.actor._
import com.sam.comm._

class PostsSender extends Actor with ActorLogging {
  var postsActors = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  override def receive: Receive = {

    case job: PostsCreateJob if postsActors.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later")

    case job: PostsGetJob if postsActors.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later")

    case job: PostsCreateJob =>
      jobCounter += 1
      postsActors(jobCounter % postsActors.size) forward job

    case job: PostsGetJob =>
      jobCounter += 1
      postsActors(jobCounter % postsActors.size) forward job

    //後端註冊服務
    case PostsActorRegister if !postsActors.contains(sender()) =>
      context watch sender()
      postsActors = postsActors :+ sender()

    //後端離開服務
    case Terminated(a) =>
      postsActors = postsActors.filterNot(_ == a)
  }
}
