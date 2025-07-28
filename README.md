# Online Whiteboard App  
  
## To deploy locally:  
### Building & packaging the app  
mvn clean package -DskipTests  
  
### Building Docker image  
docker build -t cloudboard:latest .  
  
### Running it  
docker run -p 8080:8080 cloudboard:latest  
