package com.mygdx.multiplayerdemo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.multiplayerdemo.sprites.Starship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.jar.JarEntry;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jdk.nashorn.api.scripting.JSObject;

public class MultiplayerDemo extends ApplicationAdapter {
    private final float UPDATE_TIME = 1/30f;
    private float timer;
	SpriteBatch batch;
	Texture img;
	private Socket socket;
	private String id;
	private Starship starship;
	private Texture starshipTexture, friendlyStarship;
	private HashMap<String, Starship> friendlyPlayers;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		starshipTexture = new Texture("playerShip2.png");
        friendlyStarship = new Texture("playerShip.png");
        friendlyPlayers = new HashMap<>();
		connectSocket();
		configSocketEvents();
	}

	private void handleInput(float delta){
	    if (starship != null){
	        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
	            starship.setPosition(starship.getX() + (-200 * delta), starship.getY());
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
                starship.setPosition(starship.getX() + (200 * delta), starship.getY());
            }
        }
    }

    private void updateServer(float delta){
	    timer += delta;
	    if (timer >= UPDATE_TIME && starship != null && starship.hasMoved()){
	        JSONObject data = new JSONObject();
	        try{
	            data.put("x", starship.getX());
                data.put("y", starship.getY());
                socket.emit("playerMoved", data);
            } catch(JSONException e) {
                System.out.println(e);
            }
        }
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		handleInput(Gdx.graphics.getDeltaTime());
		updateServer(Gdx.graphics.getDeltaTime());
		batch.begin();
//		batch.draw(img, 0, 0);
        if (starship != null){
            starship.draw(batch);
        }
        for (HashMap.Entry<String, Starship> entry : friendlyPlayers.entrySet()){
            entry.getValue().draw(batch);
        }
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		starshipTexture.dispose();
		friendlyStarship.dispose();
	}

	private void connectSocket(){
		try{
			socket = IO.socket("http://localhost:3000");
			socket.connect();
			System.out.println("Connecting...");
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Something went wrong...");
		}
	}

	private void configSocketEvents(){
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO", "Connected");
				starship = new Starship(starshipTexture);
			}
		}).on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try{
					id = data.getString("id");
					Gdx.app.log("SocketIO", "My id: " + id);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}).on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try{
					String playerId = data.getString("id");
					Gdx.app.log("SocketIO", "New player connected. Id: " + playerId);
					friendlyPlayers.put(playerId, new Starship(friendlyStarship));
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try{
                    id = data.getString("id");
                    friendlyPlayers.remove(id);
                } catch (JSONException e) {
                    System.out.println(e);
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try{
                    String playerId = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    if (friendlyPlayers.get(playerId) != null){
                        friendlyPlayers.get(playerId).setPosition(x.floatValue(), y.floatValue());
                    }
                } catch (JSONException e) {

                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try{
//                    for (int i = 0; i < objects.length(); i++){
                    for (int i = 0; i < objects.length(); i++){
                        Starship coopPlayer = new Starship(friendlyStarship);
                        Vector2 position = new Vector2();
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        coopPlayer.setPosition(position.x, position.y);

                        friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer);
                    }
                }catch (JSONException e){
                    System.out.println(e);
                }
            }
        });
	}
}
