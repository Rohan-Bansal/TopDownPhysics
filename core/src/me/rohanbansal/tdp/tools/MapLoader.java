package me.rohanbansal.tdp.tools;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import me.rohanbansal.tdp.events.EventSensor;
import me.rohanbansal.tdp.vehicle.Wheel;

import java.util.ArrayList;

import static me.rohanbansal.tdp.Constants.MAP_NAME;
import static me.rohanbansal.tdp.Constants.PPM;

public class MapLoader implements Disposable {

    private static final String MAP_WALL = "wall";
    private static final String MAP_EVENTS = "events";


    private World world;
    private TiledMap map;
    private ArrayList<MapObject> wallList = new ArrayList<>();
    private static ArrayList<EventSensor> eventRects = new ArrayList<>();

    public MapLoader(World world) {
        this.world = world;
    }

    public MapLoader loadMap() {
        map = new TmxMapLoader().load(MAP_NAME);

        Array<RectangleMapObject> walls = map.getLayers().get(MAP_WALL).getObjects().getByType(RectangleMapObject.class);
        Array<RectangleMapObject> events = map.getLayers().get(MAP_EVENTS).getObjects().getByType(RectangleMapObject.class);
        Array<PolygonMapObject> wallsCurved = map.getLayers().get(MAP_WALL).getObjects().getByType(PolygonMapObject.class);

        for(RectangleMapObject wall : new Array.ArrayIterator<>(walls)) {
            Rectangle rect = wall.getRectangle();
            wallList.add(wall);
            ShapeFactory.createRectangle(
                    new Vector2(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2),
                    new Vector2(rect.getWidth() / 2, rect.getHeight() / 2),
                    BodyDef.BodyType.StaticBody, world, 1f, false);
        }

        for(PolygonMapObject wallCurved: new Array.ArrayIterator<>(wallsCurved)) {
            wallList.add(wallCurved);
            ShapeFactory.createWallPolygon(wallCurved, world);
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
