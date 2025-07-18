package campo.server.route

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.impl.RouterImpl

open class ScholarRouter(val vertx: Vertx) : RouterImpl(vertx) {
    fun setLoggedIn(context: RoutingContext, user: JsonObject) {
        context.session().put("user", user)
        context.session().put("authenticated", true)
    }

    fun isLoggedIn(context: RoutingContext): Boolean {
        try {
            return context.session().get("authenticated")
        } catch(e: Throwable) {
            return false
        }
    }

    fun getEmailFromLoggedIn(context: RoutingContext): String =
        context.session().get<JsonObject>("user").getString("username")

    fun logout(context: RoutingContext) {
        context.session().put("user", null)
        context.session().put("authenticated", false)
        context.session().remove<JsonObject>("user")
        context.session().remove<Boolean>("authenticated")
    }
}