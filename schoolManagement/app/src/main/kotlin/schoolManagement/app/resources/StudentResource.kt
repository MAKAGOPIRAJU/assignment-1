package schoolManagement.app.resources

import schoolManagement.app.model.Student
import schoolManagement.app.repository.StudentRepository
import java.util.logging.Logger
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("/student")
class StudentResource @Inject constructor(var studentRepository: StudentRepository) {

  private  val logger = Logger.getLogger(StudentResource::class.java.name)

    @Path("/all")
    @GET
    fun getAllStudents() : String {

      logger.info("entering into function")

        return "All students are found";
    }

  @GET
  @Path("/all-students")
  @Produces(MediaType.APPLICATION_JSON)
  fun fetchAllStudents() : Response{

    val students = studentRepository.fetchAllStudents()

    return Response.ok(students).build();
  }


  @Path("/create")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  fun createStudent(student: Student): Response {
    val createdStudent = studentRepository.createStudent(student)
    return Response.status(Response.Status.CREATED).entity(createdStudent).build()
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  fun getStudent(@PathParam("id") id: String): Response {
    val student = studentRepository.getStudent(id) ?: return Response.status(Response.Status.NOT_FOUND).build()
    return Response.ok(student).build()
  }

  @DELETE
  @Path("{id}")
  fun deleteStudent(@PathParam("id") id: String): Response {
    val deleted = studentRepository.deleteStudent(id)

    return if (deleted) {
      Response.status(Response.Status.NO_CONTENT).build()
    } else {
      Response.status(Response.Status.NOT_FOUND).build()
    }
  }


}