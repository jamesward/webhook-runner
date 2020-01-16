package controllers

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.InjectedController
import utils.InstanceUtil
import utils.InstanceUtil.{Info, ProcessFailed}

@Singleton
class Application @Inject() extends InjectedController with Logging {

  def index = Action(parse.tolerantJson) { request =>
    request.body.validate[Info].fold({ errors =>
      BadRequest(errors.toString())
    }, { instanceInfo =>
      val createOrUpdate = InstanceUtil.describe(instanceInfo).fold({ case e: ProcessFailed =>
        logger.info("Creating instance", e)
        InstanceUtil.create(instanceInfo)
      }, { _ =>
        InstanceUtil.update(instanceInfo)
      })

      createOrUpdate.fold({ t =>
        logger.error(t.getMessage)
        InternalServerError(t.getMessage)
      }, Ok(_))
    })
  }

}
