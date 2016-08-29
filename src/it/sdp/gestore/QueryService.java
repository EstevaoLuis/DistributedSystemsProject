package it.sdp.gestore;

import it.sdp.sensori.Misurazione;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/query")
public class QueryService {

	@GET
	@Path("temprecent")
	@Produces(MediaType.TEXT_PLAIN)
	public Response TempRecent() {
		Misurazione m = DatabaseMisurazioni.getInstance().TempRecent();
		String answer = m.getValue() + "°C - time: " + m.getTimestamp()/1000;
		return Response.ok(answer).build();
	}

	@GET
	@Path("tempmedia/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response TempMedia(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			double media;
			media = DatabaseMisurazioni.getInstance().TempMedia(a, b);
			String answer ="Media: " + media + "°C";
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}

	@GET
	@Path("tempminmax/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response TempMinMax(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			double minmax[] = DatabaseMisurazioni.getInstance().TempMinMax(a, b);
			String answer ="Min: " + minmax[0] + "°C - Max: " + minmax[1] + "°C";
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}


	@GET
	@Path("lightrecent")
	@Produces(MediaType.TEXT_PLAIN)
	public Response LightRecent() {
		Misurazione m = DatabaseMisurazioni.getInstance().LightRecent();
		String answer = m.getValue() + " lux - time: " + m.getTimestamp()/1000;
		return Response.ok(answer).build();
	}

	@GET
	@Path("lightmedia/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response LightMedia(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			double media;
			media = DatabaseMisurazioni.getInstance().LightMedia(a, b);
			String answer ="Media: " + media + " lux";
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}

	@GET
	@Path("lightminmax/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response LightMinMax(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			double minmax[] = DatabaseMisurazioni.getInstance().LightMinMax(a, b);
			String answer ="Min: " + minmax[0] + " lux - Max: " + minmax[1] + " lux";
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}

	@GET
	@Path("lumtempmax/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response LumTempMax(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			Misurazione m = DatabaseMisurazioni.getInstance().LumWhereTempMax(a, b);
			String answer = m.getValue() + " lux - time: " + m.getTimestamp()/1000;
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}

	@GET
	@Path("pirest/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response PIR1Count(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			int count = DatabaseMisurazioni.getInstance().ContaPresenze(a, b, 1);
			String answer ="Presenze Est: " + count;
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}

	@GET
	@Path("pirovest/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response PIR2Count(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			int count = DatabaseMisurazioni.getInstance().ContaPresenze(a, b, 2);
			String answer ="Presenze Ovest: " + count;
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}

	@GET
	@Path("pirmedia/{a}/{b}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response PIRmedia(@PathParam("a") long a, @PathParam("b") long b) {
		try {
			int count1 = DatabaseMisurazioni.getInstance().ContaPresenze(a, b, 1);
			int count2 = DatabaseMisurazioni.getInstance().ContaPresenze(a, b, 2);
			double media = (count1 + count2) / 2.0;
			String answer ="Presenze Media: " + media;
			return Response.ok(answer).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} 
	}
}
