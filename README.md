# Online whiteboard app built with Spring
  
### To deploy locally:  
#### Build & package the app 
*mvn clean package -DskipTests*  
  
#### Build Docker image 
*docker build -t cloudboard:latest .*  
  
#### Run it 
*docker run -p 8080:8080 cloudboard:latest*  
