//Basic Vector class. Does pretty much everything you'd want a vector to do.

package util;

public class Vector 
{
	public double x, y, z;
	
	public Vector() 
	{
	}
	
	public Vector(double x, double y, double z) 
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void zero()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vector add(Vector o) {
		return new Vector(x + o.x, y + o.y, z + o.z);
	}
	
	public Vector cross(Vector o) {
		return new Vector(y * o.z - z * o.y, z*o.x - x*o.z, x*o.y - y*o.x);
	}
	
	public double dot(Vector o) {
		return x * o.x + y * o.y + x * o.z;
	}
	
	public double length() {
		return (double)Math.sqrt(x * x + y * y + z * z);
	}
	
	public double length2() {
		return x*x + y*y + z*z;
	}
	
	public Vector normalize() {
		double length = this.length();
		return new Vector(x / length, y / length, z / length);
	}
	
	public Vector scale(double scalar) {
		return new Vector(scalar * x, scalar * y, scalar * z);
	}
	
	public Vector subtract(Vector o) {
		return new Vector(x - o.x, y - o.y, z - o.z);
	}
	
	public String toString()
	{
		return "<" + x + ", " + y + ", " + z + ">";
	}
}
