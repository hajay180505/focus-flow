import json
import requests
from dotenv import load_dotenv
import os
from datetime import datetime

current_year = datetime.now().year #get current year

load_dotenv()

async def getGithubContributions(userName : str, mock: bool = True) -> dict :

  GITHUB_TOKEN =  os.getenv("GITHUB_PAT")
  if mock:
      with open("op.json","r") as f:
          try:
            content = f.read()
            return json.loads(content)
          except FileNotFoundError:
            return {"error" : "no file named op.json found", "status" : 404}
      
  if userName == "":
    return {"error" : "Invalid username", "status" : 404}
  url = "https://api.github.com/graphql"

  headers = {
      "Authorization": f"Bearer {GITHUB_TOKEN}",
      "Content-Type": "application/json",
  }

  query = """
  query($userName: String!) { 
    user(login: $userName){
      contributionsCollection {
        contributionCalendar {
          totalContributions
          weeks {
            contributionDays {
              contributionCount
              date
            }
          }
        }
      }
    }
  }
  """

  variables = {
      "userName": userName  
  }

  # Payload with both query and variables
  payload = {
      "query": query,
      "variables": variables
  }

  # Make the POST request
  response = requests.post(url, json=payload, headers=headers)

  # Check response and print results
  if response.status_code == 200:
      data = response.json()
      if "errors" in data:
          return {
             "error" : data["errors"][0]["type"],
             "status" : 404
          }
      else:
          return data
  else:
      print(f"HTTP Error: {response.status_code} - {response.text}")
      return {
         "error" : response.json()["errors"][0]["type"],
         "status" : response.status_code 
      }

def parseGithubData(data : dict, weekly :bool = False) ->dict:

    if weekly:
        curr_week = data["data"]["user"]["contributionsCollection"]["contributionCalendar"]["weeks"][-1]["contributionDays"]
        streak =  any(
            [
                x["contributionCount"] > 0
                for x in curr_week
            ]
        )
        contribs = [
            x["contributionCount"] 
                for x in curr_week
        ]
        contribs = contribs + [0] * (7 - len(contribs))
        return {
            "streak" : streak,
            "contributions" : contribs
        }
    
    contribs = data["data"]["user"]["contributionsCollection"]["contributionCalendar"]["weeks"][-1]["contributionDays"][-1]["contributionCount"] 
    return {
        "streak" : contribs > 0,
        "contributions" : contribs
    }
    
def getLeetcodeStreak(userName : str):
    if userName == "":
        return {"error" : "Invalid username", "status" : 404}
    
    url = "https://leetcode.com/graphql"  

    query = """
    query userProfileCalendar($username: String!, $year: Int) {
    matchedUser(username: $username) {
        userCalendar(year: $year) {
        activeYears
        streak
        totalActiveDays
        dccBadges {
            timestamp
            badge {
            name
            icon
            }
        }
        submissionCalendar
        }
    }
    }
    """

    variables = {
        "username": userName,
        "year": datetime.now().year
    }

    # Make the request
    response = requests.post(
        url,
        json={"query": query, "variables": variables},
        # headers=headers
    )

    # Print the response
    if response.status_code == 200:
        print(response.json())
        if "errors" in response.json():
            return {
                "error" : response.json()["errors"][0]["message"],
                "status" : 404
            }
        return response.json()
    else:
        print(f"Error {response.status_code}: {response.text}")
        return  {
         "error" : response.json()["errors"][0]["message"],
         "status" : response.status_code 
         }

def parseLeetcodeData(data : dict) -> dict:
    return {
        "streak" : data["data"]["matchedUser"]["userCalendar"]["streak"] > 0
    }
    
def getDuolingoStreak(userName: str) -> dict:
    
    def make_streak_request(userId : str):
        # url = f"https://www.duolingo.com/2017-06-30/users/{userId}?fields=streaks%7CcurrentCourse" 
        url = f"https://www.duolingo.com/2017-06-30/users/{userId}?fields=streak,streakData" 
        response = requests.get(url, headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0",
        })
        if response.status_code == 200:
            return response.json()["streakData"]["currentStreak"]["lastExtendedDate"]
        else:
            print(f"Error {response.status_code}: {response.text}")
            return  {
             "error" : response.json()["errors"][0]["message"],
             "status" : response.status_code 
             }    
    
    
    if userName == "":
        return {"error" : "Invalid username", "status" : 404}
    
    
    url = "https://www.duolingo.com/2017-06-30/users?fields=users%7Bid%7D&username=" + userName  
  
    response = requests.get(url, verify=False, headers={
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0",
    })
    
    if response.status_code == 200:
        data = response.json()
        if data["users"] == []:
            return {
                "error" : "User not found",
                "status" : 404
            }
        return {
            "streak" : datetime.strptime(make_streak_request(data["users"][0]["id"]), "%Y-%m-%d").date() == datetime.today().date()
                
            } 
        
    
    else:
        
        print(f"Error {response.status_code}: {response.text}")
        return  {
         "error" : response.json()["errors"][0]["message"],
         "status" : response.status_code 
         }