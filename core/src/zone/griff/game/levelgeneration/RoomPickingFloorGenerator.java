package zone.griff.game.levelgeneration;

public class RoomPickingFloorGenerator extends FloorGenerator {
	
  private static RoomPickingFloorGenerator instance = null;
  public static RoomPickingFloorGenerator getInstance() {
     if(instance == null) {
        instance = new RoomPickingFloorGenerator();
     }
     return instance;
  }
	
	@Override
	protected RoomGraph generateFloor(GeneratedRoom[][] roomMatrix, RoomGraph roomGraph) {
		// TODO
		return null;
	}

}
