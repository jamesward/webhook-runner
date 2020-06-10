package controllers

import java.io.File

import javax.inject.{Inject, Singleton}
import play.api.mvc.InjectedController
import play.api.{Environment, Logging}
import utils.InstanceUtil
import utils.InstanceUtil.{Info, ProcessFailed}

@Singleton
class Application @Inject()(env: Environment) extends InjectedController with Logging {

  val maybeShutdownFile: Option[File] = {
    env.resource("scripts/shutdown-on-docker-exit.sh").map { url =>
      new File(url.toURI)
    }
  }

  def index = Action(parse.tolerantJson) { request =>
    val maybeServiceAccount = request.headers.get("ServiceAccount")

    request.body.validate[Info].fold({ errors =>
      BadRequest(errors.toString())
    }, { instanceInfo =>
      val createOrUpdate = InstanceUtil.describe(instanceInfo, maybeServiceAccount).fold({ case e: ProcessFailed =>
        logger.info("Creating instance", e)
        InstanceUtil.create(instanceInfo, maybeShutdownFile, maybeServiceAccount)
      }, { _ =>
        InstanceUtil.update(instanceInfo, maybeServiceAccount).flatMap { _ =>
          InstanceUtil.start(instanceInfo, maybeServiceAccount)
        }
      })

      createOrUpdate.fold({ t =>
        logger.error(t.getMessage)
        InternalServerError(t.getMessage)
      }, Ok(_))
    })
  }

}
