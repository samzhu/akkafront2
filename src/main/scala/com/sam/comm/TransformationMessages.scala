package com.sam.comm

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

final case class Posts(text: String)

/**
  * 建立 post
  * @param posts
  */
final case class PostsCreateJob(posts: Posts)

/**
  * 取得 post
  * @param postid
  */
final case class PostsGetJob(postid: String)

/**
  *
  * @param text
  */
final case class JobFailed(text: String)

/**
  * 定義 Json 序列化跟反序列化
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val postsFormat = jsonFormat1(Posts)
}

/**
  * 後端註冊 Post 服務
  */
case object PostsActorRegister