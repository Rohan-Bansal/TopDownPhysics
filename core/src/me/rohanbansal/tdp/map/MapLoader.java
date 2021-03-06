package me.rohanbansal.tdp.map;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import me.rohanbansal.tdp.events.EventSensor;
import me.rohanbansal.tdp.tools.ShapeFactory;

import java.util.ArrayList;

import static me.rohanbansal.tdp.Constants.MAP_NAME;
import static me.rohanbansal.tdp.Constants.PPM;

public class MapLoader implements Disposable {

    private static final String MAP_WALL = "wall";
    private static final String MAP_EVENTS = "events";
    private static final String CARS = "cars";
    public static final String STATIONS = "stations";
    public static final String NODELINES = "nodelines";

    private World world;
    private TiledMap map;
    private ArrayList<MapObject> wallList = new ArrayList<>();
    private ArrayList<RectangleMapObject> carRects = new ArrayList<>();
    private static ArrayList<EventSensor> eventRects = new ArrayList<>();
    private static ArrayList<RectangleMapObject> gasStations = new ArrayList<>();
    private static ArrayList<RouteLine> nodelines = new ArrayList<>();

    public MapLoader(World world) {
        this.world = world;
    }

    public MapLoader loadMap() {
        map = new TmxMapLoader().load(MAP_NAME);

        Array<RectangleMapObject> walls = map.getLayers().get(MAP_WALL).getObjects().getByType(RectangleMapObject.class);
        Array<RectangleMapObject> events = map.getLayers().get(MAP_EVENTS).getObjects().getByType(RectangleMapObject.class);
        Array<PolygonMapObject> wallsCurved = map.getLayers().get(MAP_WALL).getObjects().getByType(PolygonMapObject.class);
        Array<RectangleMapObject> cars = map.getLayers().get(CARS).getObjects().getByType(RectangleMapObject.class);
        Array<RectangleMapObject> stations = map.getLayers().get(STATIONS).getObjects().getByType(RectangleMapObject.class);
        Array<PolylineMapObject> nodelines_ = map.getLayers().get(NODELINES).getObjects().getByType(PolylineMapObject.class);

        for(RectangleMapObject wall : new Array.ArrayIterator<>(walls)) {
            Rectangle rect = wall.getRectangle();
            wallList.add(wall);
            Body b = ShapeFactory.createRectangle(
                    new Vector2(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2),
                    new Vector2(rect.getWidth() / 2, rect.getHeight() / 2),
                    BodyDef.BodyType.StaticBody, world, 1f, false);
            b.setUserData("Wall");
        }

        for(PolygonMapObject wallCurved: new Array.ArrayIterator<>(wallsCurved)) {
            wallList.add(wallCurved);
            Body b = ShapeFactory.createWallPolygon(wallCurved, world);
            b.setUserData("Wall");
        }

        for(RectangleMapObject rect: new Array.ArrayIterator<>(cars)) {
            carRects.add(rect);
        }

        for(RectangleMapObject obj : new Array.ArrayIterator<>(stations)) {
            gasStations.add(obj);
        }

        for(PolylineMapObject rect_: new Array.ArrayIterator<>(nodelines_)) {
            Vector2 start = new Vector2(rect_.getPolyline().getTransformedVertices()[0] /  PPM, rect_.getPolyline().getTransformedVertices()[1] /  PPM);
            Vector2 end = new Vector2(rect_.getPolyline().getTransformedVertices()[2] / PPM, rect_.getPolyline().getTransformedVertices()[3] / PPM);
            nodelines.add(new RouteLine(start, end));
        }

        for(RectangleMapObject event : new Array.ArrayIterator<>(events)) {
            Rectangle rect = event.getRectangle();
            EventSensor sensor = new EventSensor(new Rectangle(rect.getX() / PPM, rect.getY() / PPM, rect.getWidth() / PPM, rect.getHeight() / PPM), event.getName());
            sensor.setBody(ShapeFactory.createRectangle(
                    new Vector2(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2),
                    new Vector2(rect.getWidth() / 2, rect.getHeight() / 2),
                    BodyDef.BodyType.StaticBody, world, 1f, true));
            eventRects.add(sensor);
        }

        return this;
    }

    public ArrayList<RouteLine> getNodelines() {
        return nodelines;
    }

    public ArrayList<RectangleMapObject> getGasStations() {
        return gasStations;
    }

    public ArrayList<RectangleMapObject> getCarRects() {
        return carRects;
    }

    public ArrayList<MapObject> getWallList() {
        return wallList;
    }

    public static ArrayList<EventSensor> getEventRects() {
        return eventRects;
    }

    public TiledMap getMap() {
        return map;
    }

    @Override
    public void dispose() {
        map.dispose();
    }
}
