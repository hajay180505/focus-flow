from typing import Optional, Union

from fastapi import FastAPI, Response
import utils

app = FastAPI()


@app.get("/")
async def read_root():
    return {"Hello": "World"}


@app.get("/items/{item_id}")
async def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}

@app.get("/github/{userName}")
async def github_request(userName : str, response : Response, weekly: Optional[bool] = False, mock : Optional[bool] = True):
    
    raw_data = await utils.getGithubContributions(userName=userName,mock=mock)

    if "error" in raw_data:
        response.status_code = raw_data["status"]
        return {"reason" : raw_data["error"]}
    return utils.parseGithubData(raw_data, weekly=weekly)
    
@app.get("/leetcode/{userName}")
async def leetcode_request(userName : str, response : Response, weekly: Optional[bool] = False):
    
    raw_data = await utils.getLeetcodeStreak(userName=userName)

    if "error" in raw_data:
        response.status_code = raw_data["status"]
        return {"reason" : raw_data["error"]}
    return utils.parseGithubData(raw_data, weekly=weekly)
    
    
    

