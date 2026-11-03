package zm.gearlog;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import zm.gearlog.exception.ItemAlreadyLentException;
import zm.gearlog.service.LendingService;

import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;

public class App {
    public static void main(String[] args) {
        LendingService service = new LendingService(Path.of("data", "gearlog.txt"));

        Javalin app = Javalin.create(config ->
                config.staticFiles.add("/public", Location.CLASSPATH));

        // Equipment
        app.get("/api/equipment", ctx -> ctx.json(service.getEquipment()));
        app.post("/api/equipment", ctx -> {
            LendingRequest r = ctx.bodyAsClass(LendingRequest.class);
            ctx.status(201).json(service.addEquipment(r.type, r.name));
        });

        // Borrowers
        app.get("/api/borrowers", ctx -> ctx.json(service.getBorrowers()));
        app.post("/api/borrowers", ctx -> {
            LendingRequest r = ctx.bodyAsClass(LendingRequest.class);
            ctx.status(201).json(service.registerBorrower(r.name, r.phone, r.group));
        });

        // Loans
        app.get("/api/loans", ctx -> ctx.json(service.getLoans()));
        app.post("/api/loans", ctx -> {
            LendingRequest r = ctx.bodyAsClass(LendingRequest.class);
            if (r.equipmentId == null || r.borrowerId == null) {
                throw new IllegalArgumentException("equipmentId and borrowerId are required");
            }
            ctx.status(201).json(service.lendItem(r.equipmentId, r.borrowerId));
        });
        app.put("/api/loans/{id}/return", ctx ->
                ctx.json(service.returnItem(Integer.parseInt(ctx.pathParam("id")))));

        // Overdue report
        app.get("/api/overdue", ctx -> ctx.json(service.getOverdue()));

        // Errors become clear messages for the front end
        app.exception(ItemAlreadyLentException.class, (e, ctx) ->
                ctx.status(409).json(Map.of("error", e.getMessage())));
        app.exception(IllegalStateException.class, (e, ctx) ->
                ctx.status(409).json(Map.of("error", e.getMessage())));
        app.exception(NoSuchElementException.class, (e, ctx) ->
                ctx.status(404).json(Map.of("error", e.getMessage())));
        app.exception(IllegalArgumentException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));

        app.start(7070);
        System.out.println("GearLog running at http://localhost:7070");
    }
}
