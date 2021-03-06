package com.codenjoy.dojo.spacerace.client.ai;

import com.codenjoy.dojo.client.Direction;
import com.codenjoy.dojo.client.LocalGameRunner;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.spacerace.client.Board;
import com.codenjoy.dojo.spacerace.model.Elements;
import com.codenjoy.dojo.spacerace.services.GameRunner;

import java.util.List;

public class AlAnTestSolver implements Solver<Board> {

	private Board board;
    private int bullets = 0;

    public AlAnTestSolver(Dice dice) {
	}

	/**
	 * Метод для запуска игры с текущим ботом. Служит для отладки.
	 */
	public static void main(String[] args) {
		LocalGameRunner.run(new GameRunner(), new AlAnTestSolver(new RandomDice()), new Board());
		// start(WebSocketRunner.DEFAULT_USER, WebSocketRunner.Host.LOCAL);
	}

	public static void start(String name, WebSocketRunner.Host server) {
		try {
			WebSocketRunner.run(server, name, new AlAnTestSolver(new RandomDice()), new Board());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String get(final Board board) {
		this.board = board;
		if (board.isGameOver())
			return "";
		Direction result = Direction.STOP;
		result = findDirection(board);
		if (result != null) {
			if(isStoneOrBombAtop() & bullets > 0){
                if(isBulletAtop()){
                    return result.toString();
                }
                bullets--;
                return result + Direction.ACT.toString();
            }
            return result.toString();
		}
		return Direction.STOP.toString();
	}

    private boolean isBulletAtop() {
        int y = board.getMe().getY();
        int x = board.getMe().getX();

        for (int i = y - 1; i >= 0; i--) {
            if(board.isBulletAt(x,i)){
                return true;
            }
        }
        return false;
    }

    private boolean isStoneOrBombAtop() {
        int y = board.getMe().getY();
        int x = board.getMe().getX();

        for (int i = y - 1; i >= 0; i--) {
            if(board.isStoneAt(x,i) || board.isBombAt(x, i)){
                return true;
            }
        }
        return false;
    }

    private Direction findDirection(Board board) {
		Direction result = Direction.STOP;

		Point me = board.getMe();
		if (me != null) {
			result = findDirectionToBulletPack(board, me, result);
		}
		return CheckResult(result, board);
	}

	private Direction findDirectionToBulletPack(Board board, Point me, Direction result) {
		List<Point> boxes = board.get(Elements.BULLET_PACK);
		if (boxes.size() != 0) {
			Point box = boxes.get(0);
			if (box != null) {
				Point newMe;
				double newDistance = (double) Integer.MAX_VALUE;
				double distance;
				newMe = new PointImpl(me.getX() + 1, me.getY());

				distance = newMe.distance(box);
				if (distance < newDistance) {
					newDistance = distance;
					result = Direction.RIGHT;
				}

				newMe = new PointImpl(me.getX(), me.getY() + 1);
				distance = newMe.distance(box);
				if (distance < newDistance) {
					newDistance = distance;
					result = Direction.DOWN;
				}

				newMe = new PointImpl(me.getX() - 1, me.getY());
				distance = newMe.distance(box);
				if (distance < newDistance) {
					newDistance = distance;
					result = Direction.LEFT;
				}

				newMe = new PointImpl(me.getX(), me.getY() - 1);
				distance = newMe.distance(box);
				if (distance < newDistance) {
					newDistance = distance;
					result = Direction.UP;
				}

                if(newDistance < 1){
                    bullets = 10;
                }
			}
		}
		return result;
	}

	private Direction CheckResult(Direction result, Board board) {
		Direction checkedResultStone = result;
		Direction checkedResultBomb = result;
        Direction checkedDirection = Direction.STOP;

		Point me = board.getMe();
		if (me != null) {

            checkedResultStone = findBestDirectionNearStone(board, me, result);
            checkedResultBomb = findBestDirectionNearBomb(board, me, result);
            //todo check condition:
            checkedResultBomb = findBestDirectionNearBomb(board, me, checkedResultBomb);

            if(checkedResultBomb.equals(result)){
                checkedDirection = checkedResultStone;
            }else {
                checkedDirection = checkedResultBomb;
            }
		}
		return checkedDirection;
	}

    private Direction findBestDirectionNearBomb(Board board, Point me, Direction givenDirection) {
        Direction bestDirection = givenDirection;

        // проход навстречу
        int x = me.getX();
        int y = me.getY();

        if ((board.isBombAt(x, y - 4)) & (bestDirection.equals(Direction.UP))){
            // TODO implement directions asap
            // посчитать дистанции вправо и влево, где меньше, то туда
            return Direction.RIGHT;
        }

        if ((board.isBombAt(x, y - 3)) & (bestDirection.equals(Direction.UP))){
            // TODO implement directions
            // посчитать дистанции справо и влево, где меньше, то туда
            return Direction.RIGHT;
        }

        if (board.isBombAt(x, y - 2)){
            // TODO implement directions
            return Direction.DOWN;
        }

        // если мина вверху справа в соседней колонке и движимся вправо или вверх, то на одну влево
        if ((board.isBombAt(x + 1, y - 3)) & // TODO implement directions
                (bestDirection.equals(Direction.RIGHT) || bestDirection.equals(Direction.UP))){
            return Direction.LEFT;
        }

        // если мина вверху справа в соседней колонке и движимся вправо или вверх, то на одну влево
        if ((board.isBombAt(x + 1, y - 2)) & // TODO implement directions
                (bestDirection.equals(Direction.RIGHT) || bestDirection.equals(Direction.UP))){
            return Direction.LEFT;
        }

        // если мина вверху справа в колонке через одну и движимся вправо, то ждем
        if ((board.isBombAt(x + 2, y - 2)) & // TODO implement directions
                (bestDirection.equals(Direction.RIGHT))){
            return Direction.STOP;
        }

        // еще ждем
        if ((board.isBombAt(x + 2, y - 1)) & // TODO implement directions
                (bestDirection.equals(Direction.RIGHT))){
            return Direction.STOP;
        }

        // еще ждем
        if ((board.isBombAt(x + 2, y)) & // TODO implement directions
                (bestDirection.equals(Direction.RIGHT))){
            return Direction.STOP;
        }

        // если мина вверху справа в колонке через одну и движимся вправо,
        // а мина уже прошла мимо,то идем дальше
        if ((board.isBombAt(x + 2, y + 1)) & // TODO implement directions
                (bestDirection.equals(Direction.RIGHT))){
            return Direction.RIGHT;
        }

        // если мина вверху слева в соседней колонке и движимся влево, то возврат на одну
        if ((board.isBombAt(x - 1, y - 3)) & // TODO implement directions
                (bestDirection.equals(Direction.LEFT) || bestDirection.equals(Direction.UP))) {
            return Direction.RIGHT;
        }

        // если мина вверху слева в соседней колонке и движимся влево, то возврат на одну
        if ((board.isBombAt(x - 1, y - 2)) & // TODO implement directions
                (bestDirection.equals(Direction.LEFT) || bestDirection.equals(Direction.UP))){
            return Direction.RIGHT;
        }

        // если мина вверху слева в колонке через одну и движимся влево, то ждем
        if ((board.isBombAt(x - 2, y - 2)) & // TODO implement directions
                (bestDirection.equals(Direction.LEFT))){
            return Direction.STOP;
        }

        // еще ждем
        if ((board.isBombAt(x - 2, y - 1)) & // TODO implement directions
                (bestDirection.equals(Direction.LEFT))){
            return Direction.STOP;
        }

        // еще ждем
        if ((board.isBombAt(x - 2, y)) & // TODO implement directions
                (bestDirection.equals(Direction.LEFT))){
            return Direction.STOP;
        }

        // если мина вверху слева в колонке через одну и движимся влево,
        // а мина уже прошла мимо,то идем дальше
        if ((board.isBombAt(x - 2, y + 1)) & // TODO implement directions
                (bestDirection.equals(Direction.LEFT))){
            return Direction.LEFT;
        }
        return bestDirection;
    }
    private Direction findBestDirectionNearStone(Board board, Point me, Direction givenDirection) {
        Direction bestDirection = givenDirection;

        if ((board.isStoneAt(me.getX() - 1, me.getY() - 1)) &
                (bestDirection.equals(Direction.LEFT))){
            return Direction.STOP;
        }

        if ((board.isStoneAt(me.getX() + 1, me.getY() - 1)) &
                (bestDirection.equals(Direction.RIGHT))){
            return Direction.STOP;
        }

        if (((board.isStoneAt(me.getX(), me.getY() - 1)) ||
                (board.isStoneAt(me.getX(), me.getY() - 2))) &
                (bestDirection.equals(Direction.UP))){
            return Direction.LEFT;
        }
        return bestDirection;
    }
}
