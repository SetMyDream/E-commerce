package storage

import storage.db.SilhouetteTableRepository

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag


@Singleton
class PasswordInfoRepository @Inject()(
      protected val dbConfigProvider: DatabaseConfigProvider,
      val tableRepository: SilhouetteTableRepository
      )(implicit ex: ExecutionContext)
      extends DelegableAuthInfoDAO[PasswordInfo]
        with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  import tableRepository._

  override val classTag = ClassTag(classOf[PasswordInfo])

  protected def searchPasswordInfo(loginInfo: LoginInfo) =
    passwordInfoQuery.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    for {
      loginInfoId <- (loginInfos returning loginInfos.map(_.id)) +=
        DBLoginInfo(None,
                    loginInfo.providerID,
                    loginInfo.providerKey.toLong)
      _ <- passwordInfoQuery +=
        DBPasswordInfo(loginInfoId,
                       authInfo.hasher,
                       authInfo.password,
                       authInfo.salt)
    } yield ()
  }.transactionally

  protected def updateAction(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    searchPasswordInfo(loginInfo)
      .map(dbPasswordInfo => (dbPasswordInfo.hasher,
                              dbPasswordInfo.password,
                              dbPasswordInfo.salt))
      .update((authInfo.hasher, authInfo.password, authInfo.salt))

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = db.run {
    val dbPasswordInfo = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbPasswordInfo <- passwordInfoQuery if dbPasswordInfo.loginInfoId === dbLoginInfo.id
    } yield dbPasswordInfo
    dbPasswordInfo.result.headOption.map(dbPIOption =>
      dbPIOption.map(dbPI =>
        PasswordInfo(dbPI.hasher, dbPI.password, dbPI.salt)))
  }

  override def save(loginInfo: LoginInfo,
                    authInfo: PasswordInfo): Future[PasswordInfo] = db.run {
    loginInfoQuery(loginInfo).joinLeft(passwordInfoQuery).on(_.id === _.loginInfoId)
    .result.head.flatMap {
      case (_, Some(_)) => updateAction(loginInfo, authInfo)
      case (_, None) => addAction(loginInfo, authInfo)
    }
  }.map(_ => authInfo)

  override def add(loginInfo: LoginInfo,
                   authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)

  override def update(loginInfo: LoginInfo,
                      authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(searchPasswordInfo(loginInfo).delete).map(_ => ())
}
