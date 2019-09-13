package controllers

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.InjectedController
import utils.InstanceUtil
import utils.InstanceUtil.{Info, ProcessFailed}

@Singleton
class Application @Inject() extends InjectedController with Logging {

  // todo: machinetype changes
  def index = Action(parse.tolerantJson) { request =>
    request.body.validate[Info].fold({ errors =>
      BadRequest(errors.toString())
    }, { instanceInfo =>
      val createOrStart = InstanceUtil.describe(instanceInfo).fold({ case _: ProcessFailed =>
        InstanceUtil.create(instanceInfo)
      }, { _ =>
        InstanceUtil.update(instanceInfo)
      })

      createOrStart.fold({ t =>
        logger.error(t.getMessage)
        InternalServerError(t.getMessage)
      }, Ok(_))
    })
  }

}
