package controllers

import models.Book
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Result}
import play.api.libs.json._
import play.mvc.{Results}
import repositories.BookRepository

import javax.inject.{Inject, Singleton}

@Singleton
class BooksController @Inject()(val controllerComponents: ControllerComponents, dataRepository: BookRepository) extends BaseController {

  def getAll: Action[AnyContent] = Action {
    Ok(Json.toJson(dataRepository.getAllBooks))
  }

  def getBook(bookId: Long): Action[AnyContent] = Action {
    var bookToReturn: Book = null
    dataRepository.getBook(bookId) foreach { book =>
      bookToReturn = book
    }

    if (bookToReturn != null) Ok(Json.toJson(bookToReturn))
    else NotFound(Json.toJson(s"The book with Id: $bookId is NOT FOUND!"))
  }

  def addBook() : Action[AnyContent] = Action {
    implicit request => {
      val requestBody = request.body
      val bookJsonObject = requestBody.asJson

      // This type of JSON un-marshalling will only work
      // if ALL fields are POSTed in the request body
      val bookItem: Option[Book] =
        bookJsonObject.flatMap(
          Json.fromJson[Book](_).asOpt
        )

      val savedBook: Option[Book] = dataRepository.addBook(bookItem.get)

      if (savedBook.isDefined) Created(Json.toJson(savedBook))
      else Conflict(Json.toJson(s"The book with Id: ${bookItem.get.id} is already exist!"))
    }
  }

  def deleteBook(bookId: Long): Action[AnyContent] = Action {
    if (dataRepository.deleteBook(bookId).getOrElse(false)) NoContent
    else NotFound(Json.toJson(s"The book with Id: $bookId is NOT FOUND!"))
  }
}
