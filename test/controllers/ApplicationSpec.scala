package controllers

import akka.stream.Materializer
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec with Results with GuiceOneAppPerSuite {

  val project = sys.env.getOrElse("PROJECT", throw new Exception("Must set PROJECT env var"))

  val maybeServiceAccount = sys.env.get("SERVICE_ACCOUNT")

  implicit lazy val materializer: Materializer = app.materializer

  "index" must {
    "create / start an app" in {
      val json = Json.obj(
        "project" -> project,
        "zone" -> "us-central1-a",
        "machineType" -> "n1-standard-1",
        "containerImage" -> "docker.io/hello-world",
      )


      val controller = app.injector.instanceOf[Application]
      val headers = maybeServiceAccount.fold(Headers())(serviceAccount => Headers("ServiceAccount" -> serviceAccount))
      val request = FakeRequest(Helpers.POST, "/").withBody(json).withHeaders(headers)
      val result = controller.index(request)
      status(result) mustEqual OK
    }
  }


}
