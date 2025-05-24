import { createTimecapsule } from '../api/timecapsuleApi';

export function useTimecapsule() {
  const createTimecapsuleAction = async form => {
    await createTimecapsule(form);
  };

  return {
    createTimecapsuleAction,
  };
}
