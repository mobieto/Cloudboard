# Online scalable whiteboard app
  
### To deploy locally:  
#### Build the app 
*mvn clean package -DskipTests*  
  
#### Build Docker image 
*docker build -t cloudboard:latest .*  
  
#### Run it 
*docker run -p 8080:8080 cloudboard:latest*  
