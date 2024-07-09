#!/usr/bin/env python
import rospy
from geometry_msgs.msg import Twist
import time 

class GoForward: 
	def forward(self, lin , ang):
		
		move_cmd = Twist()
		move_cmd.linear.x = lin 
		move_cmd.angular.z = ang
		self.cmd_vel.publish(move_cmd)
    
    def __init__(self):
		rospy.init_node('robot_cleaner', anonymous=True)
		self.cmd_vel = rospy.Publisher('/cmd_vel', Twist , queue_size=1)
		r = rospy.Rate(10);
    
        #First Rotation
        #First part of rotation (accelaration)
        t0=rospy.get_time()
        v0 = 0
        print("---Executing Rotation with accelerating angular speed for 0.349 seconds---")
        while(t0+0.349 >= rospy.get_time()):
                self.forward(0 , v0 + 0.1522687 )
            self.forward(0,0)
            
        #Second part of rotation   
        t0=0
        t0=rospy.get_time()
        print("---Executing Rotation with angular speed 0.304734 rad/sec for 1.302 seconds---")
        while(t0+1.302 >= rospy.get_time()):
                self.forward(0 , 0.304734)
            self.forward(0,0)
            
        #Third part of rotation (decelaration)
        t0=0
        v0=0.304734
        t0=rospy.get_time()
        print("---Executing Rotation with decelarating angular speed for 0.349 seconds---")
        while(t0+0.349 >= rospy.get_time()):
                self.forward(0 , v0 - 0.1522687 )
            self.forward(0,0)
            
        
        #First Linear Motion
        #First part of linear motion (accelaration)        
        t0=0
        v0=0
        t0=rospy.get_time()
        print("---Executing Linear Motion with accelerating linear speed for 1.552 seconds---")
        while(t0+1.552 >= rospy.get_time()):
            self.forward(v0+0.00776, 0)
        self.forward(0,0)
        
        #Second part of linear motion        
        t0=0
		t0=rospy.get_time()
		print("---Executing Linear Motion with linear speed 0.16 rad/sec for 111.896 seconds---")
		while(t0+111.896 >= rospy.get_time()):
			self.forward(0.16, 0)
		self.forward(0,0)
        
        #Third part of linear motion (decelaration)        
        t0=0
        v0=0.16
        t0=rospy.get_time()
        print("---Executing Linear Motion with decelarating linear speed for 1.552 seconds---")
        while(t0+1.552 >= rospy.get_time()):
            self.forward(v0-0.00776, 0)
        self.forward(0,0)
        
        #Second Rotation
        #First part of rotation (accelaration)
        t0=0
        v0=0
        t0=rospy.get_time()
        print("---Executing Rotation with accelerating angular speed for 0.385 seconds---")
        while(t0+0.385 >= rospy.get_time()):
                self.forward(0 , v0+0.2351965)
            self.forward(0,0)
            
        #Second part of rotation   
        t0=0
        t0=rospy.get_time()
        print("---Executing Rotation with angular speed 0.470366 rad/sec for 3.284 seconds---")
        while(t0+3.284 >= rospy.get_time()):
                self.forward(0 , 0.470366)
            self.forward(0,0)
            
        #Third part of rotation (decelaration)
        t0=0
        v0=0.470366
        t0=rospy.get_time()
        print("---Executing Rotation with decelarating angular speed for 0.385 seconds---")
        while(t0+0.385 >= rospy.get_time()):
                self.forward(0 , v0-0.2351965)
            self.forward(0,0)
            
        #Second Linear Motion
        #First part of linear motion (accelaration)        
        t0=0
        v0=0
        t0=rospy.get_time()
        print("---Executing Linear Motion with accelerating linear speed for 42.85 seconds---")
        while(t0+42.85 >= rospy.get_time()):
            self.forward(v0+0.0857, 0)
        self.forward(0,0)
        
        #Second part of linear motion        
        t0=0
		t0=rospy.get_time()
		print("---Executing Linear Motion with linear speed 0.17 rad/sec for 19.3 seconds---")
		while(t0+19.3 >= rospy.get_time()):
			self.forward(0.17, 0)
		self.forward(0,0)
        
        #Third part of linear motion (decelaration)        
        t0=0
        v0=0.17
        t0=rospy.get_time()
        print("---Executing Linear Motion with decelarating linear speed for 42.85 seconds---")
        while(t0+42.85 >= rospy.get_time()):
            self.forward(v0-0.0857, 0)
        self.forward(0,0)
        
        #Third Rotation
        #First part of rotation (accelaration)
        t0=0
        v0=0
        t0=rospy.get_time()
        print("---Executing Rotation with accelerating angular speed for 0.363 seconds---")
        while(t0+0.363 >= rospy.get_time()):
                self.forward(0 , v0+0.1900668)
            self.forward(0,0)
            
        #Second part of rotation   
        t0=0
        t0=rospy.get_time()
        print("---Executing Rotation with angular speed 0.380133 rad/sec for 5.274 seconds---")
        while(t0+5.274 >= rospy.get_time()):
                self.forward(0 , 0.380133)
            self.forward(0,0)
            
        #Third part of rotation (decelaration)
        t0=0
        v0=0.380133
        t0=rospy.get_time()
        print("---Executing Rotation with decelarating angular speed for 0.363 seconds---")
        while(t0+0.363 >= rospy.get_time()):
                self.forward(0 , v0-0.1900668)
            self.forward(0,0)
            
if __name__ == '__main__':
	move = GoForward()