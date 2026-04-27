package com.lcwd.user.service.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.lcwd.user.service.Exceptions.ResourceNotFoundException;
import com.lcwd.user.service.entities.Hotel;
import com.lcwd.user.service.entities.Rating;
import com.lcwd.user.service.entities.User;
import com.lcwd.user.service.external.service.HotelService;
import com.lcwd.user.service.repositories.UserRepository;
import com.lcwd.user.service.services.UserService;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private HotelService hotelservice;
	
	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	//create user
	@Override
	public User saveUser(User user) {
		// TODO Auto-generated method stub
		
		String randomUserID = UUID.randomUUID().toString();
		user.setUserId(randomUserID);
		return userRepository.save(user);
	}
	
	
	
	//get all user
	@Override
	public List<User> getAllUser() {
		// TODO Auto-generated method stub
		//return userRepository.findAll();
		
		List<User> user = userRepository.findAll();
		
		//return user;
		//return all user with rating of all hotels
		for(User user1 :user) {
			
			Rating[] usersOfratings = restTemplate.getForObject("http://RATINGSERVICE/ratings/users/"+user1.getUserId(), Rating[].class);
			logger.info("{} ",usersOfratings);
			
			List<Rating> ratings=Arrays.stream(usersOfratings).toList();
			
			List<Rating> ratinglist=ratings.stream().map(rating->{
				
				//api call to hotel service to get hotel
				//http://localhost:8082/hotels/28079bdb-5d72-4e7d-954b-fb3c778d4998
				ResponseEntity<Hotel> forEntity=restTemplate.getForEntity("http://HOTELSERVICE/hotels/"+rating.getHotelId(), Hotel.class);
				Hotel hotel=forEntity.getBody();
				logger.info("response status code : {} ",forEntity.getStatusCode());
				
				//set the hotel rating
				
				rating.setHotel(hotel);
				
				//return
				return rating;
				
			}).collect(Collectors.toList());
			
			user1.setRatings(ratinglist);
			
		}
		return user;
		
		
	}

	

	
	//get single user
	@Override
	public User getUser(String userId) {
		// TODO Auto-generated method stub
		User user =  userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with given id not found on server...:"+userId));
		
		//fetch the rating of above user using rating service
		//http://localhost:8083/ratings/users/0c989c0f-131e-4bd1-becb-fb49302f7faf
		
		Rating[] usersOfratings = restTemplate.getForObject("http://RATINGSERVICE/ratings/users/"+user.getUserId(), Rating[].class);
		logger.info("{} ",usersOfratings);
		
		List<Rating> ratings=Arrays.stream(usersOfratings).toList();
		
		List<Rating> ratinglist=ratings.stream().map(rating->{
			
			//api call to hotel service to get hotel
			//http://localhost:8082/hotels/28079bdb-5d72-4e7d-954b-fb3c778d4998
			//ResponseEntity<Hotel> forEntity=restTemplate.getForEntity("http://HOTELSERVICE/hotels/"+rating.getHotelId(), Hotel.class);
			Hotel hotel = hotelservice.getHotel(rating.getHotelId());
			
			//Hotel hotel=forEntity.getBody();
			//logger.info("response status code : {} ",forEntity.getStatusCode());
			
			//set the hotel rating
			
			rating.setHotel(hotel);
			
			//return
			return rating;
			
		}).collect(Collectors.toList());
		
		user.setRatings(ratinglist);
		
		return user;
	}

}
