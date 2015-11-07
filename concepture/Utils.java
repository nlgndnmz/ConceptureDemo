package concepture;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Implements a collection of methods to process
 * strokes. Most of these functions are based on the $1-recognizer.
 * 
 * @author Nilgun Donmez
 * @version 1.0
 */
public class Utils
{
	public static final double half_pi = Math.PI/2.0d;
	
	public static int[] getMarkers(Vector<Point> points)
	{
		int num_corners = 0;
		for(int i=0; i<points.size(); i++)
		{
			if(points.elementAt(i).breakpoint == true)
				num_corners++;
		}
		int[] markers = new int[num_corners];
		int j = 0;
		for(int i=0; i<points.size(); i++)
		{
			if(points.elementAt(i).breakpoint == true)
				markers[j++] = i;
		}
		return markers;
	}
	
	public static Vector<Point> resample(Vector<Point> srcPts, int n)
	{
		double I = PathLength(srcPts) / (n - 1); // interval length
		double D = 0.0;
		Point oldCorner = null;
		Point newCorner = null;
		
		for(int i=2; i<srcPts.size()-2; i++)
		{
			if(srcPts.elementAt(i).isBreakpoint())
			{
				oldCorner = new Point(srcPts.elementAt(i));
				break;
			}
		}

		Vector<Point> dstPts = new Vector<Point>(n);
		dstPts.addElement(new Point(srcPts.elementAt(0)));	//assumes that srcPts.size() > 0

		double best = 1000000.0;
		
		for(int i=1; i<srcPts.size(); i++)
		{
			Point pt1 = srcPts.elementAt(i - 1);
			Point pt2 = srcPts.elementAt(i);

			double d = Distance(pt1, pt2);
			if((D + d) >= I)
			{
				double qx = pt1.X + ((I - D) / d) * (pt2.X - pt1.X);
				double qy = pt1.Y + ((I - D) / d) * (pt2.Y - pt1.Y);
				Point q = new Point(qx, qy, false);				
				if(oldCorner != null)
				{
					double dist = Distance(q, oldCorner);
					if(dist < best)
					{
						newCorner = q;
						best = dist;
					}
				}
				dstPts.addElement(q); // append new point 'q'								
				srcPts.insertElementAt(q, i); // insert 'q' at position i in points s.t. 'q' will be the next i
				D = 0.0;
			}
			else
				D += d;
		}
		// sometimes we fall a rounding-error short of adding the last point
		if(dstPts.size() == n - 1)
			dstPts.addElement(new Point(srcPts.elementAt(srcPts.size() - 1)));
		if(newCorner != null)
			newCorner.setBreakpoint(true);		
		return dstPts;
	}
	
	public static Vector<Point> getSegment(Vector<Point> points, int start, int end)
	{
		Vector<Point> segment = new Vector<Point>();
		for(int i=start; i<=end; i++)
		{
			Point p = points.elementAt(i);
			segment.addElement(new Point(p));
		}
		return segment;
	}
	
	public static void sharpenAnnotations(Vector<Point> points, int k)
	{
		points.elementAt(0).breakpoint = true;
		points.elementAt(points.size()-1).breakpoint = true;
		
		if(points.size() <= k)
			return;
		        
		int vote = 0;
		for(int i=k; i<points.size()-k; i++)
		{
			int numVotes = 0;
			int total = 0;
			for(int j=-k; j<=k; j++)
			{
				if(points.elementAt(i+j).breakpoint == true)
				{
					total += (i+j);
					numVotes += 1;
				}
			}
			if(numVotes > 0)
			{
				vote = (int)((double)total / (double)numVotes);
				for(int j=-k; j<=k; j++)
					points.elementAt(i+j).breakpoint = false;
				points.elementAt(vote).breakpoint = true;
			}
		}
		
		for(int i=0; i<=k; i++)		// sometimes the beginning and ending might shift a couple of points
		{
			points.elementAt(i).breakpoint = false;
			points.elementAt(points.size()-1-i).breakpoint = false;
		}
		
		points.elementAt(0).breakpoint = true;
		points.elementAt(points.size()-1).breakpoint = true;
	}
	
	public static Vector<Point> filter(Vector<Point> points, int r)
	{		
		Vector<Point> newPoints = new Vector<Point>();
		for(int i=r; i<points.size()-r; i++)
		{		
			double x=0.0, y=0.0;
			for(int j=i-r+1; j<i+r; j++)
			{
				x += points.elementAt(j).X;
				y += points.elementAt(j).Y;				
			}				
			x = x / (2*r-1.0);
			y = y / (2*r-1.0);
			newPoints.add(new Point(x, y, false));		
		}		
		return newPoints;
	}
	
	public static double getCurvature(Point pt1, Point pt2, Point pt3)
	{
		Point p1 = new Point(pt2.X - pt1.X, pt2.Y - pt1.Y, false);
		Point p2 = new Point(pt3.X - pt2.X, pt3.Y - pt2.Y, false);
		
		double d1 = Math.sqrt( Math.pow(p1.X, 2) + Math.pow(p1.Y, 2) );
		double d2 = Math.sqrt( Math.pow(p2.X, 2) + Math.pow(p2.Y, 2) );
		
		double x = (2.0/(d1+d2)) * ( p1.X/d1 - p2.X/d2 );
		double y = (2.0/(d1+d2)) * ( p1.Y/d1 - p2.Y/d2 );
		
		return Math.sqrt( Math.pow(x, 2) + Math.pow(y, 2) );		
	}
	
	public static void findAnnotations(Vector<Point> points, int k, int t)
	{					
		double[] curvature = new double[points.size()];
		double averageCurvature = 0.0;
		double standardDeviation = 0.0;				
		for(int i=k; i<points.size()-k; i++)
		{	
			double totalLength = PathLength(points, i-k, i+k);
			double totalAngle = 0.0;			
			for(int j=1; j<=k; j++)
			{
				Point pt1 = points.elementAt(i-j);
				Point pt2 = points.elementAt(i);		
				Point pt3 = points.elementAt(i+j);
				
				Point p1 = new Point(pt2.X - pt1.X, pt2.Y - pt1.Y, false);
				Point p2 = new Point(pt3.X - pt2.X, pt3.Y - pt2.Y, false);
				
				double d1 = Math.sqrt( Math.pow(p1.X, 2) + Math.pow(p1.Y, 2) );
				double d2 = Math.sqrt( Math.pow(p2.X, 2) + Math.pow(p2.Y, 2) );
				
				double theta;
				if((p1.X/d1 * p2.X/d2) + (p1.Y/d1 * p2.Y/d2) > 0.999)					
					theta = 0.0;		
				else if((p1.X/d1 * p2.X/d2) + (p1.Y/d1 * p2.Y/d2) < -0.999)					
					theta = 3.14;
				else
					theta = Math.acos( (p1.X/d1 * p2.X/d2) + (p1.Y/d1 * p2.Y/d2) );
				
				totalAngle += theta;
			}
			totalAngle = (Math.abs(totalAngle) / totalLength);
			averageCurvature += totalAngle;
			curvature[i] = totalAngle;
		}		
		
		averageCurvature = averageCurvature / (double)(points.size() - 2*k);
		for(int i=k; i<points.size()-k; i++)
			standardDeviation += Math.pow( curvature[i] - averageCurvature, 2);
		
		standardDeviation = Math.sqrt( standardDeviation / (double)(points.size() - 2*k - 1) );
		double cutoff = 1.5*Math.max(standardDeviation,0.04) + averageCurvature;		
		
		for(int i=k; i<points.size()-k; i++)
		{					
			if(curvature[i] > cutoff)		
				points.elementAt(i).breakpoint = true;
		}		
		sharpenAnnotations(points, t);		
		findTurningPoints(points, 3, 0.1);			
	}
	
	public static Point getTangent(Point pt1, Point pt2, Point pt3)
	{
		Point p1 = new Point(pt2.X - pt1.X, pt2.Y - pt1.Y, false);
		Point p2 = new Point(pt3.X - pt2.X, pt3.Y - pt2.Y, false);
		
		double d1 = Math.sqrt( Math.pow(p1.X, 2) + Math.pow(p1.Y, 2) );
		double d2 = Math.sqrt( Math.pow(p2.X, 2) + Math.pow(p2.Y, 2) );
				
		double x = (d1*d2)/(d1+d2) * ( (p1.X / (d1*d1)) + (p2.X / (d2*d2)) ); 
		double y = (d1*d2)/(d1+d2) * ( (p1.Y / (d1*d1)) + (p2.Y / (d2*d2)) );		
		
		return new Point(x, y, false);
	}
	
	public static void findTurningPoints(Vector<Point> points, int k, double cutoff)
	{		
		Point tau = getTangent(points.elementAt(0), points.elementAt(k), points.elementAt(k+k));	
		Point rev = getTangent(points.elementAt(k+k), points.elementAt(k), points.elementAt(0));
		boolean passed = false;		
		
		for(int i=2*k+1; i<points.size()-2*k; i++)
		{											
			if(points.elementAt(i).isBreakpoint() && i+k+k<points.size())	// then reset the tangent	
			{
				tau = getTangent(points.elementAt(i), points.elementAt(i+k), points.elementAt(i+k+k));
				rev = getTangent(points.elementAt(i+k+k), points.elementAt(i+k), points.elementAt(i));
				passed = false;
			}
			else			
			{			
				Point tau2 = getTangent(points.elementAt(i-k), points.elementAt(i), points.elementAt(i+k));
				if(passed && Utils.Distance(tau, tau2) < cutoff)
				{
					points.elementAt(i).setBreakpoint(true);
					tau = getTangent(points.elementAt(i), points.elementAt(i+k), points.elementAt(i+k+k));
					rev = getTangent(points.elementAt(i+k+k), points.elementAt(i+k), points.elementAt(i));
					passed = false;
				}		
				else if(!passed && Utils.Distance(tau2, rev) < cutoff)				
					passed = true;				
			}
		}			
	}
	
	public static void findSmoothJoints(Vector<Point> points, int k, int t)
	{		
		double[] curvature = new double[points.size()];		
		for(int i=k; i<points.size()-k; i++)
		{
			double totalLength = PathLength(points, i-k, i+k);
			double totalAngle = 0.0;
			
			for(int j=1; j<=k; j++)
			{
				Point pt1 = points.elementAt(i-j);
				Point pt2 = points.elementAt(i);		
				Point pt3 = points.elementAt(i+j);
				
				Point p1 = new Point(pt2.X - pt1.X, pt2.Y - pt1.Y, false);
				Point p2 = new Point(pt3.X - pt2.X, pt3.Y - pt2.Y, false);
				
				double d1 = Math.sqrt( Math.pow(p1.X, 2) + Math.pow(p1.Y, 2) );
				double d2 = Math.sqrt( Math.pow(p2.X, 2) + Math.pow(p2.Y, 2) );
				
				double theta;
				if((p1.X/d1 * p2.X/d2) + (p1.Y/d1 * p2.Y/d2) > 0.999)
					theta = 0.0;
				else
					theta = Math.acos( (p1.X/d1 * p2.X/d2) + (p1.Y/d1 * p2.Y/d2) );
				
				totalAngle += theta;
			}
			totalAngle = (Math.abs(totalAngle) / totalLength);
			curvature[i] = totalAngle;
		}	
		
		double lim = 0.0000001;		
		for(int i=k; i<points.size()-k; i++)
		{
			double curv = curvature[i];			
			if(curv < lim)
			{
				boolean saddle = true;
				for(int j=k; j<t; j++)
				{
					if(i-j>0 && curvature[i-j] < lim)
						saddle = false;
					if(i+j<points.size() && curvature[i+j] < lim)
						saddle = false;
				}
				if(saddle)				
					points.elementAt(i).setBreakpoint(true);									
			}								
		}			
	}
	
	public static double compareMatrices(double[][] distMatrix1, double[][] distMatrix2, int size)
	{
		double rmsd = 0.0;
		for(int i=0; i<size; i++)
		{					
			for(int j=0; j<size; j++)
				rmsd += Math.pow(distMatrix1[i][j] - distMatrix2[i][j], 2);
		}
		return Math.sqrt(rmsd);
	}
	
	public static void fillDistanceMatrix(Vector<Point> points, double[][] distMatrix)
	{
		for(int i=0; i<points.size(); i++)
		{
			double ix = ((Point) points.elementAt(i)).X;
			double iy = ((Point) points.elementAt(i)).Y;
			
			for(int j=0; j<points.size(); j++)
			{
				double jx = ((Point) points.elementAt(j)).X;
				double jy = ((Point) points.elementAt(j)).Y;				
				distMatrix[i][j] = Math.sqrt( (ix - jx)*(ix - jx) + (iy - jy)*(iy - jy) );				
			}
		}
	}

	public static Vector<Point> RotateToZero(Vector<Point> points)
	{
		Point c = Centroid(points);
		Point first = points.elementAt(0);
		double theta = Math.atan2(c.Y - first.Y, c.X - first.X);
		return RotateByRadians(points, -theta);
	}

	// rotate the points by the given radians about their centroid
	public static Vector<Point> RotateByRadians(Vector<Point> points, double radians)
	{
		Point c = Centroid(points);
		return RotateAroundPoint(points, radians, c);
	}
	
	public static Vector<Point> RotateAroundPoint(Vector<Point> points, double radians, Point c)
	{		
		double Rcos = Math.cos(radians);
		double Rsin = Math.sin(radians);
		
		Vector<Point> newPoints = new Vector<Point>(points.size());
		
		Enumeration<Point> e = points.elements();
		while(e.hasMoreElements())
		{
			Point p = e.nextElement();
			double dx = p.X - c.X;
			double dy = p.Y - c.Y;				
			newPoints.addElement(new Point(dx * Rcos - dy * Rsin + c.X, 
					dx * Rsin + dy * Rcos + c.Y, p.breakpoint));
		}
		return newPoints;
	}
	
	public static void ScaleToUnit(Vector<Point> points, double size)
	{		
		size = size / PathLength(points);				
		Enumeration<Point> e = points.elements();
		while(e.hasMoreElements())
		{
			Point p = e.nextElement();
			p.X = p.X * size;
			p.Y = p.Y * size;
		}		
	}
	
	public static void ScaleToSquare(Vector<Point> points, double size)
	{
		Rectangle B = BoundingBox(points);
		size = size / (Math.max(B.Width, B.Height));
		Enumeration<Point> e = points.elements();
		while(e.hasMoreElements())
		{
			Point p = e.nextElement();
			p.X = p.X * size;
			p.Y = p.Y * size;
		}
	}

	public static void TranslateToOrigin(Vector<Point> points)
	{
		Point c = Centroid(points);
		TranslateByPoint(points, c);
	}
	
	public static void TranslateByPoint(Vector<Point> points, Point c)
	{		
		Enumeration<Point> e = points.elements();
		while(e.hasMoreElements())
		{
			Point p = e.nextElement();
			p.X = p.X - c.X;
			p.Y = p.Y - c.Y;
		}
	}
	
	public static Vector<Point> getBestOrientation(Vector<Point> prevPoints, 
			Vector<Point> currPoints, int halfWay,
			double a, double b, double threshold, double cutoff, 
			double tweak)
	{														
		Vector<Point> points = new Vector<Point>();
		Vector<Point> allpoints = new Vector<Point>();
		for(int i=0; i<currPoints.size(); i++)
		{
			if(i<halfWay)
				points.add(new Point(currPoints.elementAt(i)));
			allpoints.add(new Point(currPoints.elementAt(i)));
		}
	
		prevPoints = resample(prevPoints, halfWay);				
		
		if(false)
		{
			double size = PathLength(prevPoints);
			double oldSize = PathLength(points);
			ScaleToUnit(points, size);
			ScaleToUnit(allpoints, size * (PathLength(allpoints)/oldSize));
		}
		
		Point p = new Point(-prevPoints.elementAt(0).X + points.elementAt(0).X, 
				-prevPoints.elementAt(0).Y + points.elementAt(0).Y, false);
		TranslateByPoint(points, p);
		
		p = new Point(-prevPoints.elementAt(0).X + allpoints.elementAt(0).X, 
				-prevPoints.elementAt(0).Y + allpoints.elementAt(0).Y, false);
		TranslateByPoint(allpoints, p);
		
		double Phi = Recognizer.Phi;
		
		Point p2 = new Point(prevPoints.elementAt(0));

		double x1 = Phi * a + (1.0 - Phi) * b;
		double f1 = DistanceAtAngle(points, prevPoints, x1, p2);
		double x2 = (1.0 - Phi) * a + Phi * b;
		double f2 = DistanceAtAngle(points, prevPoints, x2, p2);

		while(Math.abs(b - a) > threshold)
		{
			if (f1 < f2)
			{
				b = x2;
				x2 = x1;
				f2 = f1;
				x1 = Phi * a + (1.0 - Phi) * b;
				f1 = DistanceAtAngle(points, prevPoints, x1, p2);
			}
			else
			{
				a = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1.0 - Phi) * a + Phi * b;
				f2 = DistanceAtAngle(points, prevPoints, x2, p2);
			}
		}
		
		double best = 1.0 - (Math.min(f1, f2) / (Recognizer.HalfDiagonal));
		if(best < cutoff)
			return null;
		
		x1 += tweak;
		x2 += tweak;
		
		if(f1 < f2)		// return the template at best angle
			return RotateAroundPoint(allpoints, x1, p2);		
		return RotateAroundPoint(allpoints, x2, p2);
	}

	public static double DistanceAtBestAngle(Vector<Point> points, 
			Vector<Point> templatePoints, double a, double b, double threshold)
	{
		double Phi = Recognizer.Phi;

		double x1 = Phi * a + (1.0 - Phi) * b;
		double f1 = DistanceAtAngle(points, templatePoints, x1);
		double x2 = (1.0 - Phi) * a + Phi * b;
		double f2 = DistanceAtAngle(points, templatePoints, x2);

		while (Math.abs(b - a) > threshold)
		{
			if (f1 < f2)
			{
				b = x2;
				x2 = x1;
				f2 = f1;
				x1 = Phi * a + (1.0 - Phi) * b;
				f1 = DistanceAtAngle(points, templatePoints, x1);
			}
			else
			{
				a = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1.0 - Phi) * a + Phi * b;
				f2 = DistanceAtAngle(points, templatePoints, x2);
			}
		}
		return Math.min(f1, f2);
	}

	public static double DistanceAtAngle(Vector<Point> points, Vector<Point> templatePoints, double theta)
	{
		Vector<Point> newpoints = RotateByRadians(points, theta);
		return PathDistance(newpoints, templatePoints);
	}
	
	public static double DistanceAtAngle(Vector<Point> points, Vector<Point> templatePoints, double theta, Point c)
	{
		Vector<Point> newpoints = RotateAroundPoint(points, theta, c);
		return PathDistance(newpoints, templatePoints);
	}

	public static Rectangle BoundingBox(Vector<Point> points)
	{
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		Enumeration<Point> e = points.elements();
		while (e.hasMoreElements())
		{
			Point p = e.nextElement();

			if (p.X < minX)
				minX = p.X;
			if (p.X > maxX)
				maxX = p.X;

			if (p.Y < minY)
				minY = p.Y;
			if (p.Y > maxY)
				maxY = p.Y;
		}
		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	public static double Distance(Point p1, Point p2)
	{
		double dx = p2.X - p1.X;
		double dy = p2.Y - p1.Y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	// compute the centroid of the points given
	public static Point Centroid(Vector<Point> points)
	{
		double xsum = 0.0;
		double ysum = 0.0;

		Enumeration<Point> e = points.elements();
		while (e.hasMoreElements())
		{
			Point p = e.nextElement();
			xsum += p.X;
			ysum += p.Y;
		}
		return new Point(xsum / points.size(), ysum / points.size(), false);
	}

	public static double PathLength(Vector<Point> points)
	{
		double length = 0.0;
		for (int i = 1; i < points.size(); i++)
			length += Distance( points.elementAt(i - 1), points.elementAt(i));
		return length;
	}
	
	public static double PathLength(Vector<Point> points, int start, int end)
	{
		double length = 0.0;
		for(int i = start+1; i <= end; i++)
			length += Distance( points.elementAt(i - 1), points.elementAt(i));
		return length;
	}

	// computes the 'distance' between two point paths by summing their corresponding point distances.	
	public static double PathDistance(Vector<Point> path1, Vector<Point> path2)
	{
		double distance = 0.0;
		for (int i = 0; i < path1.size(); i++)
		{
			distance += Distance(path1.elementAt(i), (Point) path2.elementAt(i));
		}
		return distance / path1.size();
	}
}
