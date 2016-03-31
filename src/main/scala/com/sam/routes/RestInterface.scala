package com.sam.routes

/**
  * 管理有哪些路徑可提供服務
  */
trait RestInterface extends PostsOperations {
  val routes = {
    postsRoutes
  }
}
