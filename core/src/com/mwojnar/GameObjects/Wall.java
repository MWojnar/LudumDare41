package com.mwojnar.GameObjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameEngine.Collision;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.Mask;
import com.playgon.GameEngine.MaskPart;
import com.playgon.GameEngine.MaskSurface;
import com.playgon.GameEngine.Sprite;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;
import com.playgon.Utils.LineEquation;
import com.playgon.Utils.Method;
import com.playgon.Utils.Pair;
import com.playgon.Utils.PlaygonMath;
import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;
import com.seisw.util.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

public class Wall extends Entity {
	
	private Sprite edgeSprite = null;
	private boolean refreshWall = false, edgeOutside = false, independentTiling = false, animated = true;
    private float edgeStartAngle = 0.0f, edgeEndAngle = 0.0f;
    private int currentDribbleMoveStick = -1, nonAnimatedFrame = 0;
    private Vector2 drawCalcPos = new Vector2();
	private EarClippingTriangulator earClippingTriangulator = new EarClippingTriangulator();
	private List<Pair<Pair<Sprite, float[][]>, Integer>> drawData = new ArrayList<Pair<Pair<Sprite, float[][]>, Integer>>();
	private List<Pair<Float, Float>> extraAngleData = new ArrayList<Pair<Float, Float>>();
	
	public Wall(GameWorld myWorld) {
		
		super(myWorld);
		setSprite(AssetLoader.spriteWall);
		setEdgeSprite(null);
		Mask mask = new Mask(this);
		mask.addRectangle(0.0f, 0.0f, 16.0f, 16.0f, 0);
		setMask(mask);
		setDepth(0);
		refreshWallData();
		setRotation(0);
		
	}
	
	public void setPolygonDefinition(List<Vector2> maskPolygonDefinition) {
		
		List<Integer> maskSurfaceTypes = new ArrayList<Integer>();
		if (getMask().getSurfacesOfPolygon().size() > 0) {
			
			for (MaskSurface surface : getMask().getSurfacesOfPolygon().get(0)) {
				
				maskSurfaceTypes.add(surface.getSurfaceType());
				
			}
			
		}
		
		Mask mask = new Mask(this);
		mask.addPolygon(maskPolygonDefinition);
		setMask(mask);
		
		extraAngleData.clear();
		if (getMask().getSurfacesOfPolygon().size() > 0) {
			
			if (getMask().getSurfacesOfPolygon().get(0).size() == maskSurfaceTypes.size()) {
				
				for (int i = 0; i < maskSurfaceTypes.size(); i++) {
					
					getMask().getSurfacesOfPolygon().get(0).get(i).setSurfaceType(maskSurfaceTypes.get(i));
					
				}
				
			}
			while (extraAngleData.size() < getMask().getSurfacesOfPolygon().get(0).size()) {
				
				extraAngleData.add(new Pair<Float, Float>(0.0f, 0.0f));
				
			}
			
		}
		
		refreshWallData();
		
	}
	
	@Override
	public Entity setMask(Mask mask) {
		
		super.setMask(mask);
		Rectangle boundingRect = mask.getBoundingRectangle();
		setPivot(boundingRect.x + boundingRect.width / 2.0f, boundingRect.y + boundingRect.height / 2.0f);
		return this;
		
	}
	
	@Override
	public void maskChanged() {
		
		refreshWall = true;
		
	}
	
	public void refreshWallData() {
		
		if (getSprite() != null) {
			
			drawData = new ArrayList<Pair<Pair<Sprite, float[][]>, Integer>>();
			List<List<Vector2>> polygonDefinition = getMask().getBasePointsOfPolygons();
			List<List<MaskSurface>> maskSurfaces = (getMask()).getSurfacesOfPolygon();
			List<Integer> redrawAtEnd = new ArrayList<Integer>();
			for (List<Vector2> vectorPolyPoints : polygonDefinition) {
				
				float[] maskPolyPoints = vector2ToFloatArray(vectorPolyPoints);
				PolyDefault maskPoly = new PolyDefault(), rectPoly = new PolyDefault();
				for (int i = 0; i < maskPolyPoints.length; i += 2) {
					
					maskPolyPoints[i] += getPos(false).x;
					maskPolyPoints[i + 1] += getPos(false).y;
					maskPoly.add(maskPolyPoints[i], maskPolyPoints[i + 1]);
					
				}
				if (maskPolyPoints.length > 0) {
					
					Rectangle2D boundingRect = maskPoly.getBounds();
					List<float[]> intersectingPolyList = new ArrayList<float[]>();
					float u = getSprite().getTextureRegion(0).getU(), v = getSprite().getTextureRegion(0).getV(), u2 = getSprite().getTextureRegion(0).getU2(), v2 = getSprite().getTextureRegion(0).getV2();
					float moduloBoundingX = (float) boundingRect.x, moduloBoundingY = (float) boundingRect.y;
					while (moduloBoundingX < 0) {
						
						moduloBoundingX += getSprite().getWidth();
						
					}
					while (moduloBoundingY < 0) {
						
						moduloBoundingY += getSprite().getHeight();
						
					}
					int x = (int) (boundingRect.x - moduloBoundingX % getSprite().getWidth());
					if (independentTiling) {
						
						x = (int) boundingRect.x;
						
					}
					for (; x < boundingRect.getMaxX(); x += getSprite().getWidth()) {
						
						int y = (int) (boundingRect.y - moduloBoundingY % getSprite().getHeight());
						if (independentTiling) {
							
							y = (int) boundingRect.y;
							
						}
						for (; y < boundingRect.getMaxY(); y += getSprite().getHeight()) {
							
							rectPoly.clear();
							rectPoly.add(x, y);
							rectPoly.add(x + getSprite().getWidth(), y);
							rectPoly.add(x + getSprite().getWidth(), y + getSprite().getHeight());
							rectPoly.add(x, y + getSprite().getHeight());
							Poly intersectingPoly = maskPoly.intersection(rectPoly);
							for (int i = 0; i < intersectingPoly.getNumInnerPoly(); i++) {
								
								Poly subIntersectingPoly = intersectingPoly.getInnerPoly(i);
								List<Vector2> subIntersectingPolyVectors = new ArrayList<Vector2>();
								for (int j = 0; j < subIntersectingPoly.getNumPoints(); j++) {
									
									subIntersectingPolyVectors.add(new Vector2((float) subIntersectingPoly.getX(j), (float) subIntersectingPoly.getY(j)));
									
								}
								float[] subIntersectingPolyPoints = vector2ToFloatArray(subIntersectingPolyVectors);
								if (subIntersectingPolyPoints.length != 8) {
									
									short[] subIntersectingPolyTriangles = earClippingTriangulator.computeTriangles(subIntersectingPolyPoints).toArray();
									for (int j = 0; j < subIntersectingPolyTriangles.length; j += 3) {
										
										float[] subIntersectingPolyTriangleDrawData = new float[] {
											
											subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2 + 1] - y) / (float) getSprite().getHeight(),
											subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2 + 1] - y) / (float) getSprite().getHeight(),
											subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2 + 1] - y) / (float) getSprite().getHeight(),
											subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2 + 1] - y) / (float) getSprite().getHeight()
											
										};
										intersectingPolyList.add(new float[]{(float)getAnimationSpeed()});
										intersectingPolyList.add(subIntersectingPolyTriangleDrawData);
										
									}
									
								} else {
									
									float[] subIntersectingPolyTriangleDrawData = new float[] {
										
										subIntersectingPolyPoints[0], subIntersectingPolyPoints[1], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[0] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[1] - y) / (float) getSprite().getHeight(),
										subIntersectingPolyPoints[2], subIntersectingPolyPoints[3], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[2] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[3] - y) / (float) getSprite().getHeight(),
										subIntersectingPolyPoints[4], subIntersectingPolyPoints[5], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[4] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[5] - y) / (float) getSprite().getHeight(),
										subIntersectingPolyPoints[6], subIntersectingPolyPoints[7], Color.WHITE.toFloatBits(), u + (u2 - u) * (subIntersectingPolyPoints[6] - x) / (float) getSprite().getWidth(), v2 - (v2 - v) * (subIntersectingPolyPoints[7] - y) / (float) getSprite().getHeight()
										
									};
									intersectingPolyList.add(new float[]{(float)getAnimationSpeed()});
									intersectingPolyList.add(subIntersectingPolyTriangleDrawData);
									
								}
								
							}
							
						}
						
					}
					drawData.add(new Pair<Pair<Sprite, float[][]>, Integer>(new Pair<Sprite, float[][]>(getSprite(), listFloatToFloatFloat(intersectingPolyList)), -1));
					for (int i = 0; i < polygonDefinition.get(0).size(); i++) {
						
						int pointID1 = i;
						int pointID2 = i + 1;
						int pointID3 = i + 2;
						int pointID4 = i + 3;
						if (pointID2 >= polygonDefinition.get(0).size()) {
							
							pointID2 -= polygonDefinition.get(0).size();
							
						}
						if (pointID3 >= polygonDefinition.get(0).size()) {
							
							pointID3 -= polygonDefinition.get(0).size();
							
						}
						if (pointID4 >= polygonDefinition.get(0).size()) {
							
							pointID4 -= polygonDefinition.get(0).size();
							
						}
						point1.set(polygonDefinition.get(0).get(pointID1));
						point2.set(polygonDefinition.get(0).get(pointID2));
						point3.set(polygonDefinition.get(0).get(pointID3));
						point4.set(polygonDefinition.get(0).get(pointID4));
						point1.add(getPos(false));
						point2.add(getPos(false));
						point3.add(getPos(false));
						point4.add(getPos(false));
						float animationSpeed = getAnimationSpeed();
						
						boolean skipEdge = false;
						for (Entity entity : getWorld().getEntityList()) {
							
							if (entity.getClass() == this.getClass()) {
								
								Wall wallEntity = (Wall)entity;
								List<Vector2> otherWallPolygon = wallEntity.getAbsolutePolygonDefinition();
								if (otherWallPolygon != null) {
									
									for (int id1 = 0; id1 < otherWallPolygon.size(); id1++) {
										
										int id2 = id1 + 1;
										int id3 = id1 + 2;
										int id4 = id1 + 3;
										while (id2 >= otherWallPolygon.size()) {
											
											id2 -= otherWallPolygon.size();
											
										}
										while (id3 >= otherWallPolygon.size()) {
											
											id3 -= otherWallPolygon.size();
											
										}
										while (id4 >= otherWallPolygon.size()) {
											
											id4 -= otherWallPolygon.size();
											
										}
										point5.set(otherWallPolygon.get(id1));
										point6.set(otherWallPolygon.get(id2));
										point7.set(otherWallPolygon.get(id3));
										point8.set(otherWallPolygon.get(id4));
										float angle1 = PlaygonMath.direction(point2, point1), angle2 = PlaygonMath.direction(point2, point3),
												angle3 = PlaygonMath.direction(point3, point2), angle4 = PlaygonMath.direction(point3, point4),
												angle5 = PlaygonMath.direction(point6, point5), angle6 = PlaygonMath.direction(point6, point7),
												angle7 = PlaygonMath.direction(point7, point6), angle8 = PlaygonMath.direction(point7, point8);
										if (angle2 > angle1) {
											
											angle2 -= Math.PI * 2.0f;
											
										}
										if (angle4 > angle3) {
											
											angle4 -= Math.PI * 2.0f;
											
										}
										if (angle6 > angle5) {
											
											angle6 -= Math.PI * 2.0f;
											
										}
										if (angle8 > angle7) {
											
											angle8 -= Math.PI * 2.0f;
											
										}
										float leftAngle = PlaygonMath.fixAngle(angle1 - angle2);
										float rightAngle = PlaygonMath.fixAngle(angle3 - angle4);
										float otherLeftAngle = PlaygonMath.fixAngle(angle5 - angle6);
										float otherRightAngle = PlaygonMath.fixAngle(angle7 - angle8);
										if (point6.equals(point3) && point7.equals(point2)) {
											
											skipEdge = true;
											wallEntity.removeEdgeDrawData(id2, rightAngle, leftAngle);
											if (pointID1 != 0 && !redrawAtEnd.contains(pointID1)) {
												
												redrawAtEnd.add(pointID1);
												
											}
											setExtraRightAngle(pointID1, otherRightAngle);
											setExtraLeftAngle(pointID3, otherLeftAngle);
											
										}
										
									}
									
								}
								
							}
							
						}
						
						float lineDirection = (float) (Math.atan2(point3.y - point2.y, point3.x - point2.x));
						int k = i + 1;
						if (k >= maskSurfaces.get(0).size()) {
							
							k -= maskSurfaces.get(0).size();
							
						}
						float leftAngle = 0.0f, rightAngle = 0.0f;
						if (k < extraAngleData.size()) {
							
							leftAngle = extraAngleData.get(k).getKey();
							rightAngle = extraAngleData.get(k).getValue();
							
						}
						if ((edgeStartAngle == edgeEndAngle || (PlaygonMath.withinTwoAngles(lineDirection, edgeStartAngle * (float) Math.PI / 180.0f, edgeEndAngle * (float) Math.PI / 180.0f))) && (!skipEdge)) {
							
							if (edgeSprite != null) {
								
								drawData.add(new Pair<Pair<Sprite, float[][]>, Integer>(new Pair<Sprite, float[][]>(edgeSprite, listFloatToFloatFloat(getEdgeDrawData(point1, point2, point3, point4, edgeSprite, edgeOutside, animationSpeed, leftAngle, rightAngle, getOffsetOfPolygon(edgeSprite, k)))), k));
								
							}
							
						}
						if (maskSurfaces.size() > 0) {
							
							if (k < maskSurfaces.get(0).size() ) {
								
								Sprite extraSurfaceSprite = null;
								
								if (extraSurfaceSprite != null) {
									
									drawData.add(new Pair<Pair<Sprite, float[][]>, Integer>(new Pair<Sprite, float[][]>(extraSurfaceSprite, listFloatToFloatFloat(getEdgeDrawData(point1, point2, point3, point4, extraSurfaceSprite, false, animationSpeed, leftAngle, rightAngle, getOffsetOfPolygon(extraSurfaceSprite, k)))), k));
									
								}
								
							}
							
						}
						
					}
					for (int i : redrawAtEnd) {
						
						redraw(i);
						
					}
					
				} else {
					
					drawData = new ArrayList<Pair<Pair<Sprite, float[][]>, Integer>>();
					
				}
				
			}
			setDrawCalcPos(getPos(false).cpy());
			
		} else {
			
			drawData = new ArrayList<Pair<Pair<Sprite, float[][]>, Integer>>();
			
		}
		
	}
	
	private void setExtraLeftAngle(int id, float angle) {
		
		if (id < extraAngleData.size()) {
			
			extraAngleData.get(id).setKey(angle);
			
		}
		
	}
	
	private void setExtraRightAngle(int id, float angle) {
		
		if (id < extraAngleData.size()) {
			
			extraAngleData.get(id).setValue(angle);
			
		}
		
	}
	
	private float getOffsetOfPolygon(Sprite spr, int id) {
		
		List<Vector2> polygon = getAbsolutePolygonDefinition();
		float addedDistances = 0.0f;
		if (id < polygon.size()) {
			
			for (int i = 0; i < id; i++) {
				
				addedDistances += PlaygonMath.distance(polygon.get(i), polygon.get(i + 1));
				
			}
			
		}
		
		return (Math.round(addedDistances) % spr.getWidth()) / (float) spr.getWidth();
		
	}

	@SuppressWarnings("unused")
	private void removeEdgeDrawData(int j) {
		
		for (int i = 0; i < drawData.size(); i++) {
			
			if (drawData.get(i).getValue() == j) {
				
				drawData.remove(i);
				i--;
				
			}
			
		}
		
	}
	
	private void removeEdgeDrawData(int j, float extraLeftAngle, float extraRightAngle) {
		
		int leftID = j - 1, rightID = j + 1;
		while (leftID < 0) {
			
			leftID += getPolygonDefinition().size();
			
		}
		while (rightID >= getPolygonDefinition().size()) {
			
			rightID -= getPolygonDefinition().size();
			
		}
		setExtraRightAngle(leftID, extraLeftAngle);
		setExtraLeftAngle(rightID, extraRightAngle);
		redraw(leftID);
		redraw(rightID);
		for (int i = 0; i < drawData.size(); i++) {
			
			if (drawData.get(i).getValue() == j) {
				
				drawData.remove(i);
				i--;
				
			}
			
		}
		
	}
	
	Vector2 point9 = new Vector2(), point10 = new Vector2(), point11 = new Vector2(), point12 = new Vector2();
	
	private void redraw(int id) {
		
		List<Sprite> spritesToRedraw = new ArrayList<Sprite>();
		List<Float> animationSpeeds = new ArrayList<Float>();
		for (int i = 0; i < drawData.size(); i++) {
			
			if (drawData.get(i).getValue() == id) {
				
				if (!spritesToRedraw.contains(drawData.get(i).getKey().getKey())) {
					
					spritesToRedraw.add(drawData.get(i).getKey().getKey());
					if (drawData.get(i).getKey().getValue().length > 0 && drawData.get(i).getKey().getValue()[0].length > 0) {
						
						animationSpeeds.add(drawData.get(i).getKey().getValue()[0][0]);
						
					} else {
						
						animationSpeeds.add((float)getAnimationSpeed());
						
					}
					
				}
				drawData.remove(i);
				i--;
				
			}
			
		}
		
		int id1 = id - 1;
		int id2 = id;
		int id3 = id + 1;
		int id4 = id + 2;
		while (id1 < 0) {
			
			id1 += getPolygonDefinition().size();
			
		}
		while (id3 >= getPolygonDefinition().size()) {
			
			id3 -= getPolygonDefinition().size();
			
		}
		while (id4 >= getPolygonDefinition().size()) {
			
			id4 -= getPolygonDefinition().size();
			
		}
		for (int i = 0; i < spritesToRedraw.size(); i++) {
			
			point9.set(getAbsolutePolygonDefinition().get(id1));
			point10.set(getAbsolutePolygonDefinition().get(id2));
			point11.set(getAbsolutePolygonDefinition().get(id3));
			point12.set(getAbsolutePolygonDefinition().get(id4));
			float leftAngle = 0.0f, rightAngle = 0.0f;
			if (id < extraAngleData.size()) {
				
				leftAngle = extraAngleData.get(id).getKey();
				rightAngle = extraAngleData.get(id).getValue();
				
			}
			drawData.add(new Pair<Pair<Sprite, float[][]>, Integer>(new Pair<Sprite, float[][]>(spritesToRedraw.get(i), listFloatToFloatFloat(getEdgeDrawData(point9, point10, point11, point12, spritesToRedraw.get(i), edgeOutside, animationSpeeds.get(i), leftAngle, rightAngle, getOffsetOfPolygon(spritesToRedraw.get(i), id)))), id));
			
		}
		
	}
	
	private float[][] listFloatToFloatFloat(List<float[]> listFloat) {
		
		float[][] returnFloat = new float[listFloat.size()][];
		for (int i = 0; i < listFloat.size(); i++) {
			
			returnFloat[i] = listFloat.get(i);
			
		}
		return returnFloat;
		
	}

	private List<float[]> getEdgeDrawDataOld(Vector2 point1, Vector2 point2, Vector2 point3, Vector2 point4, Sprite spr, boolean edgeOutside, float animationSpeed, float leftAddAngle, float rightAddAngle) {
		
		List<float[]> returnList = new ArrayList<float[]>();
		
		edgeMaskPoint1.set(point2);
		edgeMaskPoint2.set(point3);
		edgeMaskPoint3.set(point3.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight(),
				point3.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight());
		edgeMaskPoint4.set(point2.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight(),
				point2.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight());

		Vector2 originalEdgeMaskPoint3 = edgeMaskPoint3.cpy();
		Vector2 originalEdgeMaskPoint4 = edgeMaskPoint4.cpy();
		Vector2 tempEdgeMaskPoint3 = null;
		Vector2 tempEdgeMaskPoint4 = null;

		float angle1 = (float) Math.atan2(point1.y - point2.y, point1.x - point2.x);
		float angle2 = (float) Math.atan2(point3.y - point2.y, point3.x - point2.x);
		if (angle2 > angle1) {

			angle2 -= Math.PI * 2.0f;

		}

		boolean leftNeedsSnap = false, rightNeedsSnap = false;

		if (angle1 - angle2 < Math.PI - .01f) {

			LineEquation line1 = new LineEquation(point2, angle2 + (angle1 - angle2) / 2.0f);
			LineEquation line2 = new LineEquation(edgeMaskPoint3, edgeMaskPoint4);
			tempEdgeMaskPoint4 = line1.intersectionPointWith(line2);
			if (PlaygonMath.distance(tempEdgeMaskPoint4, edgeMaskPoint4) > spr.getWidth()) {

				float lineDirection = (new LineEquation(edgeMaskPoint2, edgeMaskPoint3)).getDirection();
				Vector2 linePoint = new Vector2(point2.x + (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth(),
				point2.y + (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth());
				line2 = new LineEquation(linePoint, lineDirection);
				tempEdgeMaskPoint4 = line1.intersectionPointWith(line2);
				leftNeedsSnap = true;
				
			}
			
		}
		
		angle1 = (float) Math.atan2(point2.y - point3.y, point2.x - point3.x);
		angle2 = (float) Math.atan2(point4.y - point3.y, point4.x - point3.x);
		if (angle2 > angle1) {
			
			angle2 -= Math.PI * 2.0f;
			
		}
		
		if (angle1 - angle2 < Math.PI - .01f) {
			
			LineEquation line1 = new LineEquation(point3, angle2 + (angle1 - angle2) / 2.0f);
			LineEquation line2 = new LineEquation(edgeMaskPoint3, edgeMaskPoint4);
			tempEdgeMaskPoint3 = line1.intersectionPointWith(line2);
			if (PlaygonMath.distance(tempEdgeMaskPoint3, edgeMaskPoint3) > spr.getWidth()) {
				
				float lineDirection = (new LineEquation(edgeMaskPoint2, edgeMaskPoint3)).getDirection();
				Vector2 linePoint = new Vector2(point3.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth(),
						point3.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth());
				line2 = new LineEquation(linePoint, lineDirection);
				tempEdgeMaskPoint3 = line1.intersectionPointWith(line2);
				rightNeedsSnap = true;
				
			}
			
		}
		
		if (tempEdgeMaskPoint3 != null) {
			
			edgeMaskPoint3 = tempEdgeMaskPoint3;
			
		}
		if (tempEdgeMaskPoint4 != null) {
			
			edgeMaskPoint4 = tempEdgeMaskPoint4;
			
		}
		
		float lineLength = PlaygonMath.distance(point2, point3);
		float lineDirection = (float) (Math.atan2(point3.y - point2.y, point3.x - point2.x) * 180.0f / Math.PI);
		float u = spr.getTextureRegion(0).getU(), v = spr.getTextureRegion(0).getV(), u2 = spr.getTextureRegion(0).getU2(), v2 = spr.getTextureRegion(0).getV2(), uDistance = u2 - u, vDistance = v2 - v;
		float startU = u, startU2 = u2, startV = v, startV2 = v2;
		float whiteColor = Color.WHITE.toFloatBits();
		if (!edgeOutside) {
			
			if (lineLength > spr.getWidth()) {
				
				Vector2 anchorPoint = new Vector2();
				float j = 0.0f;
				for (j = spr.getWidth(); j < lineLength - spr.getWidth(); j += spr.getWidth()) {
					
					anchorPoint.set(point2.x + (float) Math.cos(lineDirection * Math.PI / 180.0f) * j, point2.y + (float) Math.sin(lineDirection * Math.PI / 180.0f) * j);
					float[] positions = new float[] {
						
						anchorPoint.x, anchorPoint.y, whiteColor, u, v2,
						anchorPoint.x + (float) Math.cos((lineDirection) * Math.PI / 180.0f) * spr.getWidth(), anchorPoint.y + (float) Math.sin((lineDirection) * Math.PI / 180.0f) * spr.getWidth(), whiteColor, u2, v2,
						anchorPoint.x + (float) Math.cos((lineDirection) * Math.PI / 180.0f) * spr.getWidth() + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight(), anchorPoint.y + (float) Math.sin((lineDirection) * Math.PI / 180.0f) * spr.getWidth() + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight(), whiteColor, u2, v,
						anchorPoint.x + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight(), anchorPoint.y + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight(), whiteColor, u, v
							
					};
					returnList.add(new float[]{animationSpeed});
					returnList.add(positions);
					
				}
				anchorPoint.set(point2.x + (float) Math.cos(lineDirection * Math.PI / 180.0f) * spr.getWidth(), point2.y + (float) Math.sin(lineDirection * Math.PI / 180.0f) * spr.getWidth());
				float distanceCornerOffset = PlaygonMath.distance(edgeMaskPoint4, originalEdgeMaskPoint4);
				float tempU = u + uDistance * (distanceCornerOffset / spr.getWidth());
				if (tempU < u) {
					
					tempU = u;
					
				} else if (tempU > u2) {
					
					tempU = u2;
					
				}
				float bottomLeftPartX = anchorPoint.x + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight();
				float bottomLeftPartY = anchorPoint.y + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight();
				if (leftNeedsSnap) {
					
					bottomLeftPartX = edgeMaskPoint4.x;
					bottomLeftPartY = edgeMaskPoint4.y;
					
				}
				float[] positions = new float[] {
						
					point2.x, point2.y, whiteColor, u, v2,
					anchorPoint.x, anchorPoint.y, whiteColor, u2, v2,
					bottomLeftPartX, bottomLeftPartY, whiteColor, u2, v,
					edgeMaskPoint4.x, edgeMaskPoint4.y, whiteColor, tempU, v
						
				};
				returnList.add(new float[]{animationSpeed});
				returnList.add(positions);
				float tempLineLength = lineLength;
				while (tempLineLength > spr.getWidth()) {
					
					tempLineLength -= spr.getWidth();
					
				}
				anchorPoint.set(point3.x - (float) Math.cos(lineDirection * Math.PI / 180.0f) * tempLineLength, point3.y - (float) Math.sin(lineDirection * Math.PI / 180.0f) * tempLineLength);
				distanceCornerOffset = PlaygonMath.distance(edgeMaskPoint3, originalEdgeMaskPoint3);
				tempU =  u + uDistance * ((lineLength - j) / spr.getWidth());
				if (tempU < u) {
					
					tempU = u;
					
				} else if (tempU > u2) {
					
					tempU = u2;
					
				}
				float tempU2 =  u + uDistance * ((lineLength - j) / spr.getWidth()) - uDistance * (distanceCornerOffset / spr.getWidth());
				if (tempU2 < u) {
					
					tempU2 = u;
					
				} else if (tempU2 > u2) {
					
					tempU2 = u2;
					
				}
				float bottomRightPartX = anchorPoint.x + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight();
				float bottomRightPartY = anchorPoint.y + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight();
				if (leftNeedsSnap) {
					
					bottomRightPartX = edgeMaskPoint3.x;
					bottomRightPartY = edgeMaskPoint3.y;
					
				}
				positions = new float[] {
						
					anchorPoint.x, anchorPoint.y, whiteColor, u, v2,
					point3.x, point3.y, whiteColor, tempU, v2,
					edgeMaskPoint3.x, edgeMaskPoint3.y, whiteColor, tempU2, v,
					bottomRightPartX, bottomRightPartY, whiteColor, u, v
					
				};
				returnList.add(new float[]{animationSpeed});
				returnList.add(positions);
				
			} else {
				
				float distanceLeftCornerOffset = PlaygonMath.distance(edgeMaskPoint4, originalEdgeMaskPoint4);
				float distanceRightCornerOffset = PlaygonMath.distance(edgeMaskPoint3, originalEdgeMaskPoint3);
				float tempU = u + uDistance * (lineLength / spr.getWidth());
				if (tempU < u) {
					
					tempU = u;
					
				} else if (tempU > u2) {
					
					tempU = u2;
					
				}
				float tempU2 = u + uDistance * (lineLength / spr.getWidth()) - uDistance * (distanceRightCornerOffset / spr.getWidth());
				if (tempU2 < u) {
					
					tempU2 = u;
					
				} else if (tempU2 > u2) {
					
					tempU2 = u2;
					
				}
				float tempU3 = u + uDistance * (distanceLeftCornerOffset / spr.getWidth());
				if (tempU3 < u) {
					
					tempU3 = u;
					
				} else if (tempU3 > u2) {
					
					tempU3 = u2;
					
				}
				float[] positions = new float[] {
						
					point2.x, point2.y, whiteColor, u, v2,
					point3.x, point3.y, whiteColor, tempU, v2,
					edgeMaskPoint3.x, edgeMaskPoint3.y, whiteColor, tempU2, v,
					edgeMaskPoint4.x, edgeMaskPoint4.y, whiteColor, tempU3, v
						
				};
				returnList.add(new float[]{animationSpeed});
				returnList.add(positions);
				
			}
			
		} else {

			u = startU;
			u2 = startU2;
			v = startV - (startV - startV2) / 2.0f;
			v2 = startV2;
			Vector2 anchorPoint = new Vector2();
			float j = 0.0f;
			for (j = 0; j < lineLength - spr.getWidth(); j += spr.getWidth()) {
				
				anchorPoint.set(point2.x + (float) Math.cos(lineDirection * Math.PI / 180.0f) * j + (float) Math.cos((lineDirection - 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, point2.y + (float) Math.sin(lineDirection * Math.PI / 180.0f) * j + (float) Math.sin((lineDirection - 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f);
				float[] positions = new float[] {
					
					anchorPoint.x, anchorPoint.y, whiteColor, u, v2,
					anchorPoint.x + (float) Math.cos((lineDirection) * Math.PI / 180.0f) * spr.getWidth(), anchorPoint.y + (float) Math.sin((lineDirection) * Math.PI / 180.0f) * spr.getWidth(), whiteColor, u2, v2,
					anchorPoint.x + (float) Math.cos((lineDirection) * Math.PI / 180.0f) * spr.getWidth() + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, anchorPoint.y + (float) Math.sin((lineDirection) * Math.PI / 180.0f) * spr.getWidth() + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, whiteColor, u2, v,
					anchorPoint.x + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, anchorPoint.y + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, whiteColor, u, v
						
				};
				returnList.add(new float[]{animationSpeed});
				returnList.add(positions);
				
			}
			float tempLineLength = lineLength;
			while (tempLineLength > spr.getWidth()) {
				
				tempLineLength -= spr.getWidth();
				
			}
			anchorPoint.set(point3.x - (float) Math.cos(lineDirection * Math.PI / 180.0f) * tempLineLength, point3.y - (float) Math.sin(lineDirection * Math.PI / 180.0f) * tempLineLength);
			float tempU = u + uDistance * ((lineLength - j) / spr.getWidth());
			if (tempU < u) {
				
				tempU = u;
				
			} else if (tempU > u2) {
				
				tempU = u2;
				
			}
			float tempU2 = u + uDistance * ((lineLength - j) / spr.getWidth());
			if (tempU2 < u) {
				
				tempU2 = u;
				
			} else if (tempU2 > u2) {
				
				tempU2 = u2;
				
			}
			float[] positions = new float[] {
					
					anchorPoint.x + (float) Math.cos((lineDirection - 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, anchorPoint.y + (float) Math.sin((lineDirection - 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, whiteColor, u, v2,
					point3.x + (float) Math.cos((lineDirection - 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, point3.y + (float) Math.sin((lineDirection - 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, whiteColor, tempU, v2,
					point3.x, point3.y, whiteColor, tempU2, v,
					anchorPoint.x, anchorPoint.y, whiteColor, u, v
					
			};
			returnList.add(new float[]{animationSpeed});
			returnList.add(positions);

			u = startU;
			u2 = startU2;
			v = startV;
			v2 = startV2 + (startV - startV2) / 2.0f;

			edgeMaskPoint1.set(point2);
			edgeMaskPoint2.set(point3);
			edgeMaskPoint3.set(point3.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight() / 2.0f,
					point3.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight() / 2.0f);
			edgeMaskPoint4.set(point2.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight() / 2.0f,
					point2.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight() / 2.0f);

			originalEdgeMaskPoint3 = edgeMaskPoint3.cpy();
			originalEdgeMaskPoint4 = edgeMaskPoint4.cpy();
			tempEdgeMaskPoint3 = null;
			tempEdgeMaskPoint4 = null;

			angle1 = (float) Math.atan2(point1.y - point2.y, point1.x - point2.x);
			angle2 = (float) Math.atan2(point3.y - point2.y, point3.x - point2.x);
			if (angle2 > angle1) {

				angle2 -= Math.PI * 2.0f;

			}

			leftNeedsSnap = false;
			rightNeedsSnap = false;

			if (angle1 - angle2 < Math.PI - .01f) {

				LineEquation line1 = new LineEquation(point2, angle2 + (angle1 - angle2) / 2.0f);
				LineEquation line2 = new LineEquation(edgeMaskPoint3, edgeMaskPoint4);
				tempEdgeMaskPoint4 = line1.intersectionPointWith(line2);
				if (PlaygonMath.distance(tempEdgeMaskPoint4, edgeMaskPoint4) > spr.getWidth()) {

					lineDirection = (new LineEquation(edgeMaskPoint2, edgeMaskPoint3)).getDirection();
					Vector2 linePoint = new Vector2(point2.x + (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth(),
							point2.y + (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth());
					line2 = new LineEquation(linePoint, lineDirection);
					tempEdgeMaskPoint4 = line1.intersectionPointWith(line2);
					leftNeedsSnap = true;

				}

			}

			angle1 = (float) Math.atan2(point2.y - point3.y, point2.x - point3.x);
			angle2 = (float) Math.atan2(point4.y - point3.y, point4.x - point3.x);
			if (angle2 > angle1) {

				angle2 -= Math.PI * 2.0f;

			}

			if (angle1 - angle2 < Math.PI - .01f) {

				LineEquation line1 = new LineEquation(point3, angle2 + (angle1 - angle2) / 2.0f);
				LineEquation line2 = new LineEquation(edgeMaskPoint3, edgeMaskPoint4);
				tempEdgeMaskPoint3 = line1.intersectionPointWith(line2);
				if (PlaygonMath.distance(tempEdgeMaskPoint3, edgeMaskPoint3) > spr.getWidth()) {

					lineDirection = (new LineEquation(edgeMaskPoint2, edgeMaskPoint3)).getDirection();
					Vector2 linePoint = new Vector2(point3.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth(),
							point3.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x)) * spr.getWidth());
					line2 = new LineEquation(linePoint, lineDirection);
					tempEdgeMaskPoint3 = line1.intersectionPointWith(line2);
					rightNeedsSnap = true;

				}

			}

			if (tempEdgeMaskPoint3 != null) {

				edgeMaskPoint3 = tempEdgeMaskPoint3;

			}
			if (tempEdgeMaskPoint4 != null) {

				edgeMaskPoint4 = tempEdgeMaskPoint4;

			}

			if (lineLength > spr.getWidth()) {

				anchorPoint = new Vector2();
				j = 0.0f;
				for (j = spr.getWidth(); j < lineLength - spr.getWidth(); j += spr.getWidth()) {

					anchorPoint.set(point2.x + (float) Math.cos(lineDirection * Math.PI / 180.0f) * j, point2.y + (float) Math.sin(lineDirection * Math.PI / 180.0f) * j);
					positions = new float[] {

							anchorPoint.x, anchorPoint.y, whiteColor, u, v2,
							anchorPoint.x + (float) Math.cos((lineDirection) * Math.PI / 180.0f) * spr.getWidth(), anchorPoint.y + (float) Math.sin((lineDirection) * Math.PI / 180.0f) * spr.getWidth(), whiteColor, u2, v2,
							anchorPoint.x + (float) Math.cos((lineDirection) * Math.PI / 180.0f) * spr.getWidth() + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, anchorPoint.y + (float) Math.sin((lineDirection) * Math.PI / 180.0f) * spr.getWidth() + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, whiteColor, u2, v,
							anchorPoint.x + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, anchorPoint.y + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f, whiteColor, u, v

					};
					returnList.add(new float[]{animationSpeed});
					returnList.add(positions);

				}
				anchorPoint.set(point2.x + (float) Math.cos(lineDirection * Math.PI / 180.0f) * spr.getWidth(), point2.y + (float) Math.sin(lineDirection * Math.PI / 180.0f) * spr.getWidth());
				float distanceCornerOffset = PlaygonMath.distance(edgeMaskPoint4, originalEdgeMaskPoint4);
				tempU = u + uDistance * (distanceCornerOffset / spr.getWidth());
				if (tempU < u) {

					tempU = u;

				} else if (tempU > u2) {

					tempU = u2;

				}
				float bottomLeftPartX = anchorPoint.x + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f;
				float bottomLeftPartY = anchorPoint.y + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f;
				if (leftNeedsSnap) {

					bottomLeftPartX = edgeMaskPoint4.x;
					bottomLeftPartY = edgeMaskPoint4.y;

				}
				positions = new float[] {

						point2.x, point2.y, whiteColor, u, v2,
						anchorPoint.x, anchorPoint.y, whiteColor, u2, v2,
						bottomLeftPartX, bottomLeftPartY, whiteColor, u2, v,
						edgeMaskPoint4.x, edgeMaskPoint4.y, whiteColor, tempU, v

				};
				returnList.add(new float[]{animationSpeed});
				returnList.add(positions);
				tempLineLength = lineLength;
				while (tempLineLength > spr.getWidth()) {

					tempLineLength -= spr.getWidth();

				}
				anchorPoint.set(point3.x - (float) Math.cos(lineDirection * Math.PI / 180.0f) * tempLineLength, point3.y - (float) Math.sin(lineDirection * Math.PI / 180.0f) * tempLineLength);
				distanceCornerOffset = PlaygonMath.distance(edgeMaskPoint3, originalEdgeMaskPoint3);
				tempU =  u + uDistance * ((lineLength - j) / spr.getWidth());
				if (tempU < u) {

					tempU = u;

				} else if (tempU > u2) {

					tempU = u2;

				}
				tempU2 =  u + uDistance * ((lineLength - j) / spr.getWidth()) - uDistance * (distanceCornerOffset / spr.getWidth());
				if (tempU2 < u) {

					tempU2 = u;

				} else if (tempU2 > u2) {

					tempU2 = u2;

				}
				float bottomRightPartX = anchorPoint.x + (float) Math.cos((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f;
				float bottomRightPartY = anchorPoint.y + (float) Math.sin((lineDirection + 90) * Math.PI / 180.0f) * spr.getHeight() / 2.0f;
				if (leftNeedsSnap) {

					bottomRightPartX = edgeMaskPoint3.x;
					bottomRightPartY = edgeMaskPoint3.y;

				}
				positions = new float[] {

						anchorPoint.x, anchorPoint.y, whiteColor, u, v2,
						point3.x, point3.y, whiteColor, tempU, v2,
						edgeMaskPoint3.x, edgeMaskPoint3.y, whiteColor, tempU2, v,
						bottomRightPartX, bottomRightPartY, whiteColor, u, v

				};
				returnList.add(new float[]{animationSpeed});
				returnList.add(positions);

			} else {

				float distanceLeftCornerOffset = PlaygonMath.distance(edgeMaskPoint4, originalEdgeMaskPoint4);
				float distanceRightCornerOffset = PlaygonMath.distance(edgeMaskPoint3, originalEdgeMaskPoint3);
				tempU = u + uDistance * (lineLength / spr.getWidth());
				if (tempU < u) {

					tempU = u;

				} else if (tempU > u2) {

					tempU = u2;

				}
				tempU2 = u + uDistance * (lineLength / spr.getWidth()) - uDistance * (distanceRightCornerOffset / spr.getWidth());
				if (tempU2 < u) {

					tempU2 = u;

				} else if (tempU2 > u2) {

					tempU2 = u2;

				}
				float tempU3 = u + uDistance * (distanceLeftCornerOffset / spr.getWidth());
				if (tempU3 < u) {

					tempU3 = u;

				} else if (tempU3 > u2) {

					tempU3 = u2;

				}
				positions = new float[] {

						point2.x, point2.y, whiteColor, u, v2,
						point3.x, point3.y, whiteColor, tempU, v2,
						edgeMaskPoint3.x, edgeMaskPoint3.y, whiteColor, tempU2, v,
						edgeMaskPoint4.x, edgeMaskPoint4.y, whiteColor, tempU3, v

				};
				returnList.add(new float[]{animationSpeed});
				returnList.add(positions);

			}
			
		}
		return returnList;
		
	}
	
	private Vector2 offsetPoint1 = new Vector2(), offsetPoint2 = new Vector2(), offsetPoint3 = new Vector2(), offsetPoint4 = new Vector2();
	
	private List<float[]> getEdgeDrawData(Vector2 point1, Vector2 point2, Vector2 point3, Vector2 point4, Sprite spr, boolean edgeOutside, float animationSpeed, float leftAddAngle, float rightAddAngle, float uOffset) {
		
		if (edgeOutside) {
			
			return getEdgeDrawDataOld(point1, point2, point3, point4, spr, edgeOutside, animationSpeed, leftAddAngle, rightAddAngle);
			
		}
		
		List<float[]> returnList = new ArrayList<float[]>();
		
		edgeMaskPoint1.set(point2);
		edgeMaskPoint2.set(point3);
		edgeMaskPoint3.set(point3.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight(),
											point3.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight());
		edgeMaskPoint4.set(point2.x - (float) Math.cos(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight(),
											point2.y - (float) Math.sin(Math.atan2(point3.y - point2.y, point3.x - point2.x) - Math.PI / 2.0f) * spr.getHeight());
		
		Vector2 originalMaskPoint3 = edgeMaskPoint3.cpy();
		Vector2 originalMaskPoint4 = edgeMaskPoint4.cpy();
		Vector2 tempEdgeMaskPoint3 = null;
		Vector2 tempEdgeMaskPoint4 = null;
		
		float angle1 = (float) Math.atan2(point1.y - point2.y, point1.x - point2.x);
		float angle2 = (float) Math.atan2(point3.y - point2.y, point3.x - point2.x);
		if (angle2 > angle1) {
			
			angle2 -= Math.PI * 2.0f;
			
		}
		
		float wholeLeftAngle = angle1 - angle2 + leftAddAngle;
		if (wholeLeftAngle < Math.PI - .01f) {
			
			LineEquation line1 = new LineEquation(point2, angle2 + (wholeLeftAngle) / 2.0f);
			LineEquation line2 = new LineEquation(edgeMaskPoint3, edgeMaskPoint4);
			tempEdgeMaskPoint4 = line1.intersectionPointWith(line2);
			
		}
		
		angle1 = (float) Math.atan2(point2.y - point3.y, point2.x - point3.x);
		angle2 = (float) Math.atan2(point4.y - point3.y, point4.x - point3.x);
		if (angle2 > angle1) {
			
			angle2 -= Math.PI * 2.0f;
			
		}
		
		float wholeRightAngle = angle1 - angle2 + rightAddAngle;
		if (wholeRightAngle < Math.PI - .01f) {
			
			LineEquation line1 = new LineEquation(point3, angle2 + (wholeRightAngle) / 2.0f);
			LineEquation line2 = new LineEquation(edgeMaskPoint3, edgeMaskPoint4);
			tempEdgeMaskPoint3 = line1.intersectionPointWith(line2);
			
		}
		
		if (tempEdgeMaskPoint3 != null) {
			
			edgeMaskPoint3 = tempEdgeMaskPoint3;
			
		}
		if (tempEdgeMaskPoint4 != null) {
			
			edgeMaskPoint4 = tempEdgeMaskPoint4;
			
		}
		
		float distance = PlaygonMath.distance(edgeMaskPoint1, edgeMaskPoint2);
		float direction = PlaygonMath.direction(edgeMaskPoint1, edgeMaskPoint2);
		Vector2 point3Coords = PlaygonMath.getCoordsOnRotatedGrid(edgeMaskPoint1, direction, edgeMaskPoint3);
		Vector2 point4Coords = PlaygonMath.getCoordsOnRotatedGrid(edgeMaskPoint1, direction, edgeMaskPoint4);
		if (point4Coords.x > point3Coords.x) {
			
			LineEquation line1 = new LineEquation(edgeMaskPoint1, edgeMaskPoint4);
			LineEquation line2 = new LineEquation(edgeMaskPoint2, edgeMaskPoint3);
			edgeMaskPoint3.set(line1.intersectionPointWith(line2));
			edgeMaskPoint4.set(edgeMaskPoint3);
			
		}
		
		PolyDefault maskPoly = new PolyDefault(), rectPoly = new PolyDefault();
		maskPoly.add(edgeMaskPoint1.x, edgeMaskPoint1.y);
		maskPoly.add(edgeMaskPoint2.x, edgeMaskPoint2.y);
		maskPoly.add(edgeMaskPoint3.x, edgeMaskPoint3.y);
		maskPoly.add(edgeMaskPoint4.x, edgeMaskPoint4.y);
		float u = spr.getTextureRegion(0).getU(), v = spr.getTextureRegion(0).getV(), u2 = spr.getTextureRegion(0).getU2(), v2 = spr.getTextureRegion(0).getV2();
		for (int x = 0; x < distance; x += spr.getWidth()) {
			
			offsetPoint1.set(0.0f, 0.0f);
			offsetPoint2.set(0.0f, 0.0f);
			offsetPoint3.set(0.0f, 0.0f);
			offsetPoint4.set(0.0f, 0.0f);
			float currentOffset = 0.0f;
			if (x == 0 && uOffset > 0 && uOffset < 1) {
				
				offsetPoint1 = edgeMaskPoint1.cpy();
				offsetPoint2 = edgeMaskPoint1.cpy().add(PlaygonMath.getGridVector(spr.getWidth() * (1 - uOffset), direction));
				offsetPoint3 = originalMaskPoint4.cpy().add(PlaygonMath.getGridVector(spr.getWidth() * (1 - uOffset), direction));
				offsetPoint4 = originalMaskPoint4.cpy();
				currentOffset = uOffset;
				x -= spr.getWidth() - (spr.getWidth() * (1 - uOffset)) + 1;
				uOffset = 0.0f;
				
			} else if (x + spr.getWidth() > distance) {
				
				offsetPoint1 = edgeMaskPoint1.cpy().add(PlaygonMath.getGridVector(x, direction));
				offsetPoint2 = edgeMaskPoint2.cpy();
				offsetPoint3 = originalMaskPoint3.cpy();
				offsetPoint4 = originalMaskPoint4.cpy().add(PlaygonMath.getGridVector(x, direction));
				
			} else {
				
				offsetPoint1 = edgeMaskPoint1.cpy().add(PlaygonMath.getGridVector(x, direction));
				offsetPoint2 = edgeMaskPoint1.cpy().add(PlaygonMath.getGridVector(x + spr.getWidth(), direction));
				offsetPoint3 = originalMaskPoint4.cpy().add(PlaygonMath.getGridVector(x + spr.getWidth(), direction));
				offsetPoint4 = originalMaskPoint4.cpy().add(PlaygonMath.getGridVector(x, direction));
				
			}
			rectPoly.clear();
			rectPoly.add(offsetPoint1.x, offsetPoint1.y);
			rectPoly.add(offsetPoint2.x, offsetPoint2.y);
			rectPoly.add(offsetPoint3.x, offsetPoint3.y);
			rectPoly.add(offsetPoint4.x, offsetPoint4.y);
			Poly intersectingPoly = maskPoly.intersection(rectPoly);
			for (int i = 0; i < intersectingPoly.getNumInnerPoly(); i++) {
				
				Poly subIntersectingPoly = intersectingPoly.getInnerPoly(i);
				List<Vector2> subIntersectingPolyVectors = new ArrayList<Vector2>();
				for (int j = 0; j < subIntersectingPoly.getNumPoints(); j++) {
					
					subIntersectingPolyVectors.add(new Vector2((float) subIntersectingPoly.getX(j), (float) subIntersectingPoly.getY(j)));
					
				}
				float[] subIntersectingPolyPoints = vector2ToFloatArray(subIntersectingPolyVectors);
				if (subIntersectingPolyPoints.length != 8) {
					
					short[] subIntersectingPolyTriangles = earClippingTriangulator.computeTriangles(subIntersectingPolyPoints).toArray();
					for (int j = 0; j < subIntersectingPolyTriangles.length; j += 3) {
						
						Vector2 coordsPoint1 = PlaygonMath.getCoordsOnRotatedGrid(offsetPoint1, direction, new Vector2(subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2 + 1]));
						Vector2 coordsPoint2 = PlaygonMath.getCoordsOnRotatedGrid(offsetPoint1, direction, new Vector2(subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2 + 1]));
						Vector2 coordsPoint3 = PlaygonMath.getCoordsOnRotatedGrid(offsetPoint1, direction, new Vector2(subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2 + 1]));
						float[] subIntersectingPolyTriangleDrawData = new float[] {
							
							subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint1.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint1.y / (float) spr.getHeight()),
							subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 1] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint2.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint2.y / (float) spr.getHeight()),
							subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j + 2] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint3.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint3.y / (float) spr.getHeight()),
							subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2], subIntersectingPolyPoints[subIntersectingPolyTriangles[j] * 2 + 1], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint1.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint1.y / (float) spr.getHeight())
							
						};
						returnList.add(new float[]{animationSpeed});
						returnList.add(subIntersectingPolyTriangleDrawData);
						
					}
					
				} else {
					
					Vector2 coordsPoint1 = PlaygonMath.getCoordsOnRotatedGrid(offsetPoint1, direction, new Vector2(subIntersectingPolyPoints[0], subIntersectingPolyPoints[1]));
					Vector2 coordsPoint2 = PlaygonMath.getCoordsOnRotatedGrid(offsetPoint1, direction, new Vector2(subIntersectingPolyPoints[2], subIntersectingPolyPoints[3]));
					Vector2 coordsPoint3 = PlaygonMath.getCoordsOnRotatedGrid(offsetPoint1, direction, new Vector2(subIntersectingPolyPoints[4], subIntersectingPolyPoints[5]));
					Vector2 coordsPoint4 = PlaygonMath.getCoordsOnRotatedGrid(offsetPoint1, direction, new Vector2(subIntersectingPolyPoints[6], subIntersectingPolyPoints[7]));
					float[] subIntersectingPolyTriangleDrawData = new float[] {
						
						subIntersectingPolyPoints[0], subIntersectingPolyPoints[1], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint1.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint1.y / (float) spr.getHeight()),
						subIntersectingPolyPoints[2], subIntersectingPolyPoints[3], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint2.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint2.y / (float) spr.getHeight()),
						subIntersectingPolyPoints[4], subIntersectingPolyPoints[5], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint3.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint3.y / (float) spr.getHeight()),
						subIntersectingPolyPoints[6], subIntersectingPolyPoints[7], Color.WHITE.toFloatBits(), u + (u2 - u) * (coordsPoint4.x / (float) spr.getWidth() + currentOffset), v2 - (v2 - v) * (coordsPoint4.y / (float) spr.getHeight())
						
					};
					returnList.add(new float[]{animationSpeed});
					returnList.add(subIntersectingPolyTriangleDrawData);
					
				}
				
			}
			
		}
		
		return returnList;
		
	}
	
	public void setAbsolutePolygonDefinition(List<Vector2> polygonDefinition) {
		
		for (int i = 0; i < polygonDefinition.size(); i++) {
			
			polygonDefinition.get(i).sub(getPos(false));
			
		}
		setPolygonDefinition(polygonDefinition);
		
	}
	
	public Sprite getEdgeSprite() {
		
		return edgeSprite;
		
	}
	
	public void setEdgeSprite(Sprite edgeSprite) {
		
		this.edgeSprite = edgeSprite;
		
	}
	
	public List<Vector2> getPolygonDefinition() {
		
		List<List<Vector2>> polygonDefinition = getMask().getBasePointsOfPolygons();
		if (polygonDefinition.size() > 0) {
			
			return polygonDefinition.get(0);
			
		}
		
		return new ArrayList<Vector2>();
		
	}
	
	public List<Vector2> getAbsolutePolygonDefinition() {
		
		List<List<Vector2>> polygonDefinition = getMask().getBasePointsOfPolygons();
		if (polygonDefinition.size() > 0) {
			
			for (int i = 0; i < polygonDefinition.get(0).size(); i++) {
				
				polygonDefinition.get(0).get(i).add(getPos(false));
				
			}
			return polygonDefinition.get(0);
			
		}
		
		return new ArrayList<Vector2>();
		
	}
	
	@Override
	public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
		
		super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);
		
		if (refreshWall) {
			
			refreshWallData();
			refreshWall = false;
			
		}
		
	}
	
	private Vector2 point1 = new Vector2(), point2 = new Vector2(), point3 = new Vector2(), point4 = new Vector2(),
			point5 = new Vector2(), point6 = new Vector2(), point7 = new Vector2(), point8 = new Vector2();
	private Rectangle screenBoundaries = new Rectangle();
	private Rectangle expandedScreenBoundaries = new Rectangle();
	private Rectangle translatedBoundingRectangle = new Rectangle();
	private Vector2 offsetVector = new Vector2();
	private Vector2 centerVector = new Vector2();

	@Override
	public void draw(GameRenderer renderer) {

		//In case update wasn't called because active == false
		if (refreshWall) {

			refreshWallData();
			refreshWall = false;

		}

		screenBoundaries.set(getWorld().getCamPos(false).x, getWorld().getCamPos(false).y, getWorld().getGameDimensions().x, getWorld().getGameDimensions().y);
		if (getEdgeSprite() != null) {

			expandedScreenBoundaries.set(screenBoundaries.x - getEdgeSprite().getHeight(), screenBoundaries.y - getEdgeSprite().getHeight(), screenBoundaries.width + getEdgeSprite().getHeight() * 2.0f, screenBoundaries.height + getEdgeSprite().getHeight() * 2.0f);

		} else {

			expandedScreenBoundaries.set(screenBoundaries);

		}
		translatedBoundingRectangle.set(getPos(false).x + getMask().getBoundingRectangle().x, getPos(false).y + getMask().getBoundingRectangle().y, getMask().getBoundingRectangle().width, getMask().getBoundingRectangle().height);
		if (expandedScreenBoundaries.overlaps(translatedBoundingRectangle)) {

			float animationSpeed = getAnimationSpeed();
			if (animationSpeed <= 0.0f) {

				animationSpeed = 15.0f;

			}

			offsetVector.set(getPos(false).x - getDrawCalcPos().x, getPos(false).y - getDrawCalcPos().y);
			centerVector.set(getDrawCalcPos().x + getPos(true).x - getPos(false).x, getDrawCalcPos().y + getPos(true).y - getPos(false).y);
			if (offsetVector.x != 0 || offsetVector.y != 0) {

				if (getRotation() != 0) {

					for (Pair<Pair<Sprite, float[][]>, Integer> individualEdgeDrawData : drawData) {

						drawAll(individualEdgeDrawData.getKey().getValue(), offsetVector, centerVector, getRotation(), renderer, screenBoundaries, individualEdgeDrawData.getKey().getKey());

					}

				} else {

					for (Pair<Pair<Sprite, float[][]>, Integer> individualEdgeDrawData : drawData) {

						drawAll(individualEdgeDrawData.getKey().getValue(), offsetVector, renderer, screenBoundaries, individualEdgeDrawData.getKey().getKey());

					}

				}

			} else if (getRotation() != 0) {

				for (Pair<Pair<Sprite, float[][]>, Integer> individualEdgeDrawData : drawData) {

					drawAll(individualEdgeDrawData.getKey().getValue(), centerVector, getRotation(), renderer, screenBoundaries, individualEdgeDrawData.getKey().getKey());

				}

			} else {

				for (Pair<Pair<Sprite, float[][]>, Integer> individualEdgeDrawData : drawData) {

					drawAll(individualEdgeDrawData.getKey().getValue(), renderer, screenBoundaries, individualEdgeDrawData.getKey().getKey());

				}

			}

		}

		if (getWorld().isDebug()) {

			renderer.getBatcher().end();
			renderer.getShapeRenderer().begin(ShapeType.Filled);

			renderer.getShapeRenderer().setColor(Color.RED);
			renderer.getShapeRenderer().circle(getPos(true).x, getPos(true).y, 8);

			renderer.getShapeRenderer().end();
			renderer.getBatcher().begin();
			Sprite tempSprite = getSprite();
			setSprite(null);
			super.draw(renderer);
			setSprite(tempSprite);

		}

	}
	
	private int drawAll(float[][] positions, GameRenderer renderer, Rectangle trueScreenBoundaries, Sprite sprite) {
		
		int boundaryExtension = Math.max(sprite.getWidth(), sprite.getHeight());
		screenBoundaries.set(trueScreenBoundaries.x - boundaryExtension, trueScreenBoundaries.y - boundaryExtension, trueScreenBoundaries.width + boundaryExtension * 2.0f, trueScreenBoundaries.height + boundaryExtension * 2.0f);
		
		float leftBound = screenBoundaries.x;
		float rightBound = screenBoundaries.x + screenBoundaries.width;
		float topBound = screenBoundaries.y;
		float bottomBound = screenBoundaries.y + screenBoundaries.height;
		SpriteBatch batch = renderer.getBatcher();
		int amountDrawn = 0;
		for (int i = 0; i < positions.length; i+= 2) {
			
			int frame = nonAnimatedFrame;
			float xOffset = 0.0f;
			if (sprite.getFrames() > 0 && !getWorld().isDebug() && animated) {
				
				if (positions[i][0] > 0) {
					
					frame = (int) ((positions[i][0] / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / positions[i][0])));
					
				} else if (positions[i][0] < 0) {
					
					frame = sprite.getFrames() - 1 - (int) ((Math.abs(positions[i][0]) / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / Math.abs(positions[i][0]))));
					
				}
				
			}
			xOffset = frame * ((float) sprite.getWidth()) / sprite.getTexture().getWidth();
			
			if (frame > 0) {
				
				positions[i + 1][3] += xOffset;
				positions[i + 1][8] += xOffset;
				positions[i + 1][13] += xOffset;
				positions[i + 1][18] += xOffset;
				
			}
			if (leftBound <= positions[i + 1][0] && rightBound >= positions[i + 1][0] && topBound <= positions[i + 1][1] && bottomBound >= positions[i + 1][1]/*screenBoundaries.contains(positions[i + 1][0], positions[i + 1][1])*/) {
				
				batch.draw(sprite.getTexture(), positions[i + 1], 0, 20);
				amountDrawn++;
				
			}
			if (frame > 0) {
				
				positions[i + 1][3] -= xOffset;
				positions[i + 1][8] -= xOffset;
				positions[i + 1][13] -= xOffset;
				positions[i + 1][18] -= xOffset;
				
			}
			
		}
		return amountDrawn;
		
	}

	private int drawAll(float[][] positions, Vector2 offset, GameRenderer renderer, Rectangle trueScreenBoundaries, Sprite sprite) {
		
		int boundaryExtension = Math.max(sprite.getWidth(), sprite.getHeight());
		screenBoundaries.set(trueScreenBoundaries.x - boundaryExtension, trueScreenBoundaries.y - boundaryExtension, trueScreenBoundaries.width + boundaryExtension * 2.0f, trueScreenBoundaries.height + boundaryExtension * 2.0f);
		
		float leftBound = screenBoundaries.x;
		float rightBound = screenBoundaries.x + screenBoundaries.width;
		float topBound = screenBoundaries.y;
		float bottomBound = screenBoundaries.y + screenBoundaries.height;
		SpriteBatch batch = renderer.getBatcher();
		int amountDrawn = 0;
		for (int i = 0; i < positions.length; i+= 2) {
			
			int frame = nonAnimatedFrame;
			float xOffset = 0.0f;
			if (sprite.getFrames() > 0 && !getWorld().isDebug() && animated) {
				
				if (positions[i][0] > 0) {
					
					frame = (int) ((positions[i][0] / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / positions[i][0])));
					
				} else if (positions[i][0] < 0) {
					
					frame = sprite.getFrames() - 1 - (int) ((Math.abs(positions[i][0]) / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / Math.abs(positions[i][0]))));
					
				}
				
			}
			xOffset = frame * ((float) sprite.getWidth()) / sprite.getTexture().getWidth();
			
			positions[i + 1][0] += offset.x;
			positions[i + 1][1] += offset.y;
			positions[i + 1][5] += offset.x;
			positions[i + 1][6] += offset.y;
			positions[i + 1][10] += offset.x;
			positions[i + 1][11] += offset.y;
			positions[i + 1][15] += offset.x;
			positions[i + 1][16] += offset.y;
			if (frame > 0) {
				
				positions[i + 1][3] += xOffset;
				positions[i + 1][8] += xOffset;
				positions[i + 1][13] += xOffset;
				positions[i + 1][18] += xOffset;
				
			}
			if (leftBound <= positions[i + 1][0] && rightBound >= positions[i + 1][0] && topBound <= positions[i + 1][1] && bottomBound >= positions[i + 1][1]/*screenBoundaries.contains(positions[i + 1][0], positions[i + 1][1])*/) {
				
				batch.draw(sprite.getTexture(), positions[i + 1], 0, 20);
				amountDrawn++;
				
			}
			if (frame > 0) {
				
				positions[i + 1][3] -= xOffset;
				positions[i + 1][8] -= xOffset;
				positions[i + 1][13] -= xOffset;
				positions[i + 1][18] -= xOffset;
				
			}
			positions[i + 1][0] -= offset.x;
			positions[i + 1][1] -= offset.y;
			positions[i + 1][5] -= offset.x;
			positions[i + 1][6] -= offset.y;
			positions[i + 1][10] -= offset.x;
			positions[i + 1][11] -= offset.y;
			positions[i + 1][15] -= offset.x;
			positions[i + 1][16] -= offset.y;
			
		}
		return amountDrawn;
		
	}
	
	private int drawAll(float[][] positions, Vector2 centerPos, float rotation, GameRenderer renderer, Rectangle trueScreenBoundaries, Sprite sprite) {
		
		int boundaryExtension = Math.max(sprite.getWidth(), sprite.getHeight());
		screenBoundaries.set(trueScreenBoundaries.x - boundaryExtension, trueScreenBoundaries.y - boundaryExtension, trueScreenBoundaries.width + boundaryExtension * 2.0f, trueScreenBoundaries.height + boundaryExtension * 2.0f);
		
		float leftBound = screenBoundaries.x;
		float rightBound = screenBoundaries.x + screenBoundaries.width;
		float topBound = screenBoundaries.y;
		float bottomBound = screenBoundaries.y + screenBoundaries.height;
		SpriteBatch batch = renderer.getBatcher();
		int amountDrawn = 0;
		float cosValue = (float) Math.cos(rotation);
		float sinValue = (float) Math.sin(rotation);
		for (int i = 0; i < positions.length; i+= 2) {
			
			int frame = nonAnimatedFrame;
			float xOffset = 0.0f;
			if (sprite.getFrames() > 0 && !getWorld().isDebug() && animated) {
				
				if (positions[i][0] > 0) {
					
					frame = (int) ((positions[i][0] / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / positions[i][0])));
					
				} else if (positions[i][0] < 0) {
					
					frame = sprite.getFrames() - 1 - (int) ((Math.abs(positions[i][0]) / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / Math.abs(positions[i][0]))));
					
				}
				
			}
			xOffset = frame * ((float) sprite.getWidth()) / sprite.getTexture().getWidth();
			
			float previousX1 = positions[i + 1][0];
			float previousY1 = positions[i + 1][1];
			float previousX2 = positions[i + 1][5];
			float previousY2 = positions[i + 1][6];
			float previousX3 = positions[i + 1][10];
			float previousY3 = positions[i + 1][11];
			float previousX4 = positions[i + 1][15];
			float previousY4 = positions[i + 1][16];
			float newYPos = centerPos.y + (positions[i + 1][0] - centerPos.x) * sinValue + (positions[i + 1][1] - centerPos.y) * cosValue;
			positions[i + 1][0] = centerPos.x + (positions[i + 1][0] - centerPos.x) * cosValue - (positions[i + 1][1] - centerPos.y) * sinValue;
			positions[i + 1][1] = newYPos;
			newYPos = centerPos.y + (positions[i + 1][5] - centerPos.x) * sinValue + (positions[i + 1][6] - centerPos.y) * cosValue;
			positions[i + 1][5] = centerPos.x + (positions[i + 1][5] - centerPos.x) * cosValue - (positions[i + 1][6] - centerPos.y) * sinValue;
			positions[i + 1][6] = newYPos;
			newYPos = centerPos.y + (positions[i + 1][10] - centerPos.x) * sinValue + (positions[i + 1][11] - centerPos.y) * cosValue;
			positions[i + 1][10] = centerPos.x + (positions[i + 1][10] - centerPos.x) * cosValue - (positions[i + 1][11] - centerPos.y) * sinValue;
			positions[i + 1][11] = newYPos;
			newYPos = centerPos.y + (positions[i + 1][15] - centerPos.x) * sinValue + (positions[i + 1][16] - centerPos.y) * cosValue;
			positions[i + 1][15] = centerPos.x + (positions[i + 1][15] - centerPos.x) * cosValue - (positions[i + 1][16] - centerPos.y) * sinValue;
			positions[i + 1][16] = newYPos;
			if (frame > 0) {
				
				positions[i + 1][3] += xOffset;
				positions[i + 1][8] += xOffset;
				positions[i + 1][13] += xOffset;
				positions[i + 1][18] += xOffset;
				
			}
			if (leftBound <= positions[i + 1][0] && rightBound >= positions[i + 1][0] && topBound <= positions[i + 1][1] && bottomBound >= positions[i + 1][1]/*screenBoundaries.contains(positions[i + 1][0], positions[i + 1][1])*/) {
				
				batch.draw(sprite.getTexture(), positions[i + 1], 0, 20);
				amountDrawn++;
				
			}
			if (frame > 0) {
				
				positions[i + 1][3] -= xOffset;
				positions[i + 1][8] -= xOffset;
				positions[i + 1][13] -= xOffset;
				positions[i + 1][18] -= xOffset;
				
			}
			positions[i + 1][0] = previousX1;
			positions[i + 1][1] = previousY1;
			positions[i + 1][5] = previousX2;
			positions[i + 1][6] = previousY2;
			positions[i + 1][10] = previousX3;
			positions[i + 1][11] = previousY3;
			positions[i + 1][15] = previousX4;
			positions[i + 1][16] = previousY4;
			
		}
		return amountDrawn;
		
	}

	private int drawAll(float[][] positions, Vector2 offset, Vector2 centerPos, float rotation, GameRenderer renderer, Rectangle trueScreenBoundaries, Sprite sprite) {
		
		int boundaryExtension = Math.max(sprite.getWidth(), sprite.getHeight());
		screenBoundaries.set(trueScreenBoundaries.x - boundaryExtension, trueScreenBoundaries.y - boundaryExtension, trueScreenBoundaries.width + boundaryExtension * 2.0f, trueScreenBoundaries.height + boundaryExtension * 2.0f);
		
		float leftBound = screenBoundaries.x;
		float rightBound = screenBoundaries.x + screenBoundaries.width;
		float topBound = screenBoundaries.y;
		float bottomBound = screenBoundaries.y + screenBoundaries.height;
		SpriteBatch batch = renderer.getBatcher();
		int amountDrawn = 0;
		for (int i = 0; i < positions.length; i+= 2) {
			
			int frame = nonAnimatedFrame;
			float xOffset = 0.0f;
			if (sprite.getFrames() > 0 && !getWorld().isDebug() && animated) {
				
				if (positions[i][0] > 0) {
					
					frame = (int) ((positions[i][0] / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / positions[i][0])));
					
				} else if (positions[i][0] < 0) {
					
					frame = sprite.getFrames() - 1 - (int) ((Math.abs(positions[i][0]) / 60.0f) * (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % (sprite.getFrames() * 60.0f / Math.abs(positions[i][0]))));
					
				}
				
			}
			xOffset = frame * ((float) sprite.getWidth()) / sprite.getTexture().getWidth();
			
			positions[i + 1][0] += offset.x;
			positions[i + 1][1] += offset.y;
			positions[i + 1][5] += offset.x;
			positions[i + 1][6] += offset.y;
			positions[i + 1][10] += offset.x;
			positions[i + 1][11] += offset.y;
			positions[i + 1][15] += offset.x;
			positions[i + 1][16] += offset.y;
			if (frame > 0) {
				
				positions[i + 1][3] += xOffset;
				positions[i + 1][8] += xOffset;
				positions[i + 1][13] += xOffset;
				positions[i + 1][18] += xOffset;
				
			}
			if (leftBound <= positions[i + 1][0] && rightBound >= positions[i + 1][0] && topBound <= positions[i + 1][1] && bottomBound >= positions[i + 1][1]/*screenBoundaries.contains(positions[i + 1][0], positions[i + 1][1])*/) {
				
				batch.draw(sprite.getTexture(), positions[i + 1], 0, 20);
				amountDrawn++;
				
			}
			if (frame > 0) {
				
				positions[i + 1][3] -= xOffset;
				positions[i + 1][8] -= xOffset;
				positions[i + 1][13] -= xOffset;
				positions[i + 1][18] -= xOffset;
				
			}
			positions[i + 1][0] -= offset.x;
			positions[i + 1][1] -= offset.y;
			positions[i + 1][5] -= offset.x;
			positions[i + 1][6] -= offset.y;
			positions[i + 1][10] -= offset.x;
			positions[i + 1][11] -= offset.y;
			positions[i + 1][15] -= offset.x;
			positions[i + 1][16] -= offset.y;
			
		}
		return amountDrawn;
		
	}

	private Vector2 edgeMaskPoint1 = new Vector2(), edgeMaskPoint2 = new Vector2(), edgeMaskPoint3 = new Vector2(), edgeMaskPoint4 = new Vector2();
	
	public boolean isEdgeOutside() {
		
		return edgeOutside;
		
	}

	public void setEdgeOutside(boolean edgeOutside) {
		
		this.edgeOutside = edgeOutside;
		
	}

	@Override
	protected void loadLevelEditorMethods() {
		
		super.loadLevelEditorMethods();
		
		for (Pair<Method, Method> methods : levelEditorMethods) {
			
			if (methods.getKey().getLevelEditorDescriptionText() == "Sprite ") {
				
				methods.getKey().setValueList(AssetLoader.spriteWallBackgroundList);
				break;
				
			}
			
		}
		levelEditorMethods.add(new Pair<Method, Method>(new Method(Wall.class, "setAbsolutePolygonDefinition", "Defining Polygon ", new ArrayList<Vector2>()), new Method(Wall.class, "getAbsolutePolygonDefinition")));
		levelEditorMethods.add(new Pair<Method, Method>(new Method(Wall.class, "setEdgeStartAngle", "Start Angle ", 0.0f), new Method(Wall.class, "getEdgeStartAngle")));
		levelEditorMethods.add(new Pair<Method, Method>(new Method(Wall.class, "setEdgeEndAngle", "End Angle ", 0.0f), new Method(Wall.class, "getEdgeEndAngle")));
		
	}
	
	@Override
	public boolean checkRegion() {
		
		if (super.checkRegion()) {
			
			return true;
			
		}
		
		boolean isActive = false;
		if (getMask().getPointsOfPolygons().size() > 0 && getWorld().isUsingRegions()) {
			
			List<Vector2> polygonPoints = getMask().getPointsOfPolygons().get(0);
			for (int i = 0; i < polygonPoints.size(); i++) {
				
				if (i >= polygonPoints.size() - 1) {
					
					if (!getWorld().outsideRegion(polygonPoints.get(i).cpy().add(getPos(false)), polygonPoints.get(0).cpy().add(getPos(false)))) {
						
						isActive = true;
						break;
						
					}
					
				} else {
					
					if (!getWorld().outsideRegion(polygonPoints.get(i).cpy().add(getPos(false)), polygonPoints.get(i + 1).cpy().add(getPos(false)))) {
						
						isActive = true;
						break;
						
					}
					
				}
				
			}
			
			for (Vector2 polygonPoint : polygonPoints) {
				
				if (!getWorld().outsideRegion(polygonPoint.cpy().add(getPos(false)))) {
					
					isActive = true;
					break;
					
				}
				
			}
			
		}
		return isActive;
		
	}
	
	public float getEdgeStartAngle() {
		
		return edgeStartAngle;
		
	}
	
	public void setEdgeStartAngle(float edgeStartAngle) {
		
		this.edgeStartAngle = edgeStartAngle;
		
	}
	
	public float getEdgeEndAngle() {
		
		return edgeEndAngle;
		
	}
	
	public void setEdgeEndAngle(float edgeEndAngle) {
		
		this.edgeEndAngle = edgeEndAngle;
		
	}

	public Vector2 getDrawCalcPos() {
		
		return drawCalcPos;
		
	}

	public void setDrawCalcPos(Vector2 drawCalcPos) {
		
		this.drawCalcPos = drawCalcPos;
		
	}
	
	public byte[] floatToBytes(float theFloat) {
		
		float f = theFloat;
        byte[] op = new byte[4];
        int fi = Float.floatToIntBits(f);
        for (int i = 0; i < 4; i++)
        {
        	
            int offset = (op.length - 1 - i) * 8;
            op[i] = (byte) ((fi >>> offset) & 0xff);
            
        }
        return op;
		
	}
	
	public float bytesToFloat(byte[] bytes) {
		
		int asInt = (bytes[0] & 0xFF) 
	            | ((bytes[1] & 0xFF) << 8) 
	            | ((bytes[2] & 0xFF) << 16) 
	            | ((bytes[3] & 0xFF) << 24);
		return (Float.intBitsToFloat(asInt));
		
	}

	public void setCurrentDribbleMoveStick(MaskPart collideePart) {
		
		currentDribbleMoveStick = -1;
		if (getMask().getPointsOfPolygons().size() > 0 && collideePart instanceof MaskSurface) {
			
			List<Vector2> pointsOfPolygon = getMask().getPointsOfPolygons().get(0);
			for (int i = 0; i < pointsOfPolygon.size(); i++) {
				
				if (((MaskSurface)collideePart).getPoint1().equals(pointsOfPolygon.get(i))) {
					
					currentDribbleMoveStick = i;
					break;
					
				}
				
			}
			
		}
		
	}
	
	public void setIndependentTiling(boolean independentTiling) {
		
		this.independentTiling = independentTiling;
		
	}
	
	public boolean isIndependentTiling() {
		
		return independentTiling;
		
	}
	
	public boolean isAnimated() {
		
		return animated;
		
	}
	
	public void setAnimated(boolean animated) {
		
		this.animated = animated;
		
	}
	
	public int getNonAnimatedFrame() {
		
		return nonAnimatedFrame;
		
	}
	
	public void setNonAnimatedFrame(int nonAnimatedFrame) {
		
		this.nonAnimatedFrame = nonAnimatedFrame;
		
	}

	public static float[] vector2ToFloatArray(List<Vector2> list) {

		float[] returnFloat = new float[list.size() * 2];
		for (int i = 0; i < list.size(); i++) {

			returnFloat[i * 2] = list.get(i).x;
			returnFloat[i * 2 + 1] = list.get(i).y;

		}

		return returnFloat;

	}
	
}