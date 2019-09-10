package controllers

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{InjectedController, WebSocket}
import utils.InstanceUtil.InstanceInfo

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class Application @Inject() extends InjectedController with Logging {

  def index = Action(parse.json) { request =>

    request.body.validate[InstanceInfo].fold({ errors =>
      BadRequest(errors.toString())
    }, { instanceInfo =>

      NotImplemented
    })
  }

}
