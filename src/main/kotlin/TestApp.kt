import com.tenebras.spero.DbConnectionManager
import com.tenebras.spero.Repository
import com.tenebras.spero.SperoApp
import com.tenebras.spero.route.*
import java.time.ZonedDateTime

fun main(args: Array<String>) {

    SperoApp {
        injection { DbConnectionManager::class with ::ConnectionManager }

        routes {
            "/hello/{:\\d+}-{:\\d+}-{second:\\d+}" with Controller::numbers
            "/hello/{name:[a-z]+}" with Controller::hello
            "/hello" with Controller::index
        }
    }

//        bind(DbConnectionManager("jdbc:postgresql://localhost/money"))
//        bind(::DbConnectionManager)
//        bind<DbConnectionManager>()
//        DbConnectionManager::class with ::ConnectionManager
//        DbConnectionManager::class with DbConnectionManager("jdbc:postgresql://localhost/money")
//        ProfileRepository::class with ProfileRepository(instance())


//val ap = app.injector.instance<SperoApp>()


//    val profileRepository = app.injector.instance<ProfileRepository>()
//
//    profileRepository.all()
    println("Hello")
}

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class ConfigValue(val name: String)


open class ProfileRepository(connectionManager: DbConnectionManager) : Repository<Profile>(::Profile, connectionManager)
class Rep(connectionManager: DbConnectionManager) : ProfileRepository(connectionManager)

class ConnectionManager(connectionString: String = "jdbc:postgresql://localhost/money") : DbConnectionManager(connectionString)

class Profile(var id: String, var email: String, var password: String, var createdAt: ZonedDateTime, var lastLoginAt: ZonedDateTime)
class ProfileTransformer(val profile: Profile)


class Controller {
    fun hello() = ""
    fun numbers(r: Request) = ""
    fun index() = Response("Hello")
}

