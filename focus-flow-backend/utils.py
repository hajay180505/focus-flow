import json
import requests
from dotenv import load_dotenv
import os
from datetime import datetime
import time

epoch_time = int(time.time()) #epoch time

current_year = datetime.now().year #get current year

normal_time = datetime.fromtimestamp(epoch_time) #epoch to normal

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
        return response.json()
    else:
        print(f"Error {response.status_code}: {response.text}")
        return  {
         "error" : response.json()["errors"][0]["message"],
         "status" : response.status_code 
         }

def parseLeetcodeData(data : dict, weekly: bool =False) -> dict:
    raise NotImplementedError("Not implemented yet")