package it.sdp.gestore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

@Path("/user")
public class UserService {

	@POST
	@Path("add")
	@Consumes(javax.ws.rs.core.MediaType.APPLICATION_XML)
	public Response insertUser(JAXBElement<User> u) {
		if (Userlist.getInstance().TryAdd(u.getValue()))	   
			return Response.ok().build();
		return Response.status(Response.Status.CONFLICT).build();
	}

	@DELETE
	@Path("delete/{name}")
	public Response deleteUser(@PathParam("name") String user) {
		if (Userlist.getInstance().Delete(user))
			return Response.ok().build();
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}
