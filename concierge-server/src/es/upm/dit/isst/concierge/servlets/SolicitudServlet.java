package es.upm.dit.isst.concierge.servlets;

import es.upm.dit.isst.concierge.dao.ClienteDAOImplementation;
import es.upm.dit.isst.concierge.dao.EmpleadoDAOImplementation;
import es.upm.dit.isst.concierge.dao.MensajeDAOImplementation;
import es.upm.dit.isst.concierge.dao.SolicitudDAOImplementation;
import es.upm.dit.isst.concierge.model.Cliente;
import es.upm.dit.isst.concierge.model.Empleado;
import es.upm.dit.isst.concierge.model.Mensaje;
import es.upm.dit.isst.concierge.model.Solicitud;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

@WebServlet("/solicitud")
public class SolicitudServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean loggedin = req.getSession().getAttribute("loggedin") != null &&
                            (boolean) req.getSession().getAttribute("loggedin");
        JsonObject jsonObject;
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Cliente c = loggedin ?
                (req.getSession().getAttribute("client")!= null?
                        (Cliente)req.getSession().getAttribute("client"):null)
                :null;
        try {
            if(loggedin && c != null){
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = req.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String data = buffer.toString();
                JsonReader jsonReader = Json.createReader(new StringReader(data));
                jsonObject = jsonReader.readObject();
                
                // Choose one member of the staff randomly
                
                List<Empleado> empleados = (List<Empleado>) EmpleadoDAOImplementation.getInstance().readAll();
                Random random = new Random();
                int randomInt = random.nextInt(empleados.size())+1;
                Empleado e = EmpleadoDAOImplementation.getInstance().read(randomInt);
                
                // Create request
                Solicitud s = new Solicitud();
                s.setTitulo(jsonObject.getString("titulo"));
                s.setCliente(c);
                s.setEmpleado(e);
                s.setEstado("Pendiente");
                SolicitudDAOImplementation.getInstance().create(s);
                
               // Create first message from client
                Mensaje m = new Mensaje();
                m.setEmisorCliente(true);
                m.setSolicitud(s);
                m.setCuerpo(jsonObject.getString("mensaje"));
                m.setTimestamp( new Timestamp(System.currentTimeMillis()));
                MensajeDAOImplementation.getInstance().create(m);
                
                // Create first message from staff member
                //TimeUnit.MINUTES.sleep(1);
                Mensaje m2 = new Mensaje();
                m2.setEmisorCliente(false);
                m2.setSolicitud(s);
                m2.setCuerpo("Estimado/a "+c.getNombre()+", "+e.getName()+" se encargara de procesar esta solicitud. Le agradecemos su espera.");
                m2.setTimestamp( new Timestamp(System.currentTimeMillis() + 60 * 1000));
                MensajeDAOImplementation.getInstance().create(m2);
                
                Cliente actualizado = ClienteDAOImplementation.getInstance().read(c.getDni());
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                String json = mapper.writeValueAsString(actualizado);
                jsonObject = Json.createObjectBuilder()
                        .add("code",200)
                        .add("cliente", json)
                        .build();
                out.print(jsonObject.toString());
                req.getSession().setAttribute("client", actualizado);
            } else {
                jsonObject = Json.createObjectBuilder()
                        .add("code",401)
                        .build();
                out.print(jsonObject.toString());
            }
        }catch (Exception e){
            System.out.println(e);
            jsonObject = Json.createObjectBuilder()
                    .add("code",401)
                    .add("excepcion", e.toString())
                    .build();
            out.print(jsonObject.toString());
        }
        out.flush();

    }
}
