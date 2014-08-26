ROSJava
=======

Java implementation of ROS Core services (fork) certified on Raspberry Pi

This project is a fork of the original Apache 2.0 Licensed project with the major changes being the removal 
of 'Google Collections' dependency and their custom assertion framework in favor of standard Java collections
and 'assert' keyword. Also added additional logic in onNodeReplacement to attempt to more robustly reconnect when a 
new node replaces a previous one. None of the tools of ROS are within scope of this, but Core, that being Master and 
Parameter server and supporting messages and their plumbing. The intent was to reduce the codebase rather than address
any other issues. Also removed are the test harnesses and other tooling regarding generation of Catkin and Gradle-based
packages to produce executable JARs which are ROS 'packages'. Ant is used in each of the subprojects to do builds. 
Eclipse was the development environment used.  The original project was borken down into several subprojects for 
easier handling: 
ROSJava - this. 
ROSCore - brings up core server, rosrun shortcut, 
ROSMsgs - generated messages from bootstrap GenerateInterfaces, 
ROSMsgsGeom - geometry as above, 
ROSBase - org.ros interface bindings pre-generated Java classes
Also not included is the XmlRpcServer Apache source which was in the original project. Issues were encountered 
with current binary in official distro so the code may be reposted here, or a JAR. There are numerous dependencies
on third party libs, primarily Apache XmlRPC and Netty which are addressed in the documentation here:
http://hackaday.io/project/1784-ROSCOE---Open-Source%2C-Work-Rated%2C-Autonomous-Robot


Portions released under Apache V2.0:
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
