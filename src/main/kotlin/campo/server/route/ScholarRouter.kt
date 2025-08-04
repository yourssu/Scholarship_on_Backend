package campo.server.route

import campo.server.data.User
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.impl.RouterImpl

open class ScholarRouter(vertx: Vertx) : RouterImpl(vertx) {
    fun setLoggedIn(context: RoutingContext, user: JsonObject) {
        context.session().put("user", user)
        context.session().put("authenticated", true)
    }

    fun setUserInfo(context: RoutingContext, user: User) {
        context.session().put("user", user)
        context.session().put("authenticated", true)
    }

    fun getUserInfo(context: RoutingContext) : User? {
        return context.session().get("user")
    }

    fun isLoggedIn(context: RoutingContext): Boolean {
        try {
            return context.session().get("authenticated")
        } catch(e: Throwable) {
            return false
        }
    }

    fun getEmailFromLoggedIn(context: RoutingContext): String {
        return try {
            context.session().get<JsonObject>("user").getString("username")
        } catch(e: Exception) {
            context.session().get<User>("user").email
        }
    }

    fun logout(context: RoutingContext) {
        context.session().put("user", null)
        context.session().put("authenticated", false)
        context.session().remove<JsonObject>("user")
        context.session().remove<Boolean>("authenticated")
    }
}